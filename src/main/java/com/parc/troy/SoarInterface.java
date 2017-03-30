package com.parc.troy;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;

import com.parc.xi.dm.Config;
import com.parc.xi.dm.DispatchCallback;
import com.parc.xi.dm.LogicalForm;
import com.parc.xi.dm.agent.DialogRuleFn;
import com.parc.xi.dm.state.DialogState;
import com.parc.xi.dm.state.Plan;

import static com.parc.xi.dm.LogicalFormConstants.*;
import sml.Agent;
import sml.Identifier;
import sml.IdentifierSymbol;
import sml.Kernel;
import sml.WMElement;
import sml.sml;
import sml.smlRunEventId;
import sml.Agent.RunEventInterface;
import sml.smlRhsEventId;


public class SoarInterface implements DialogRuleFn, RunEventInterface {
	private Kernel kernel;
	private Agent troySoarAgent;
	private String troySource;
	private Identifier inputLink;
	private Identifier outputLink;
	private Identifier interactionLink;
	
	private boolean isRunning = false;
	private boolean queueStop = false;
	
	private LogicalForm messageToWrite;
	private DialogState dialogStateToCallback;
	private LogicalForm callToProcess;

	public SoarInterface(String name, String dmName){
		this.configureAndStartSoarAgent(name, dmName);
	}
	
	void configureAndStartSoarAgent(String name, String dmName)
	{
		this.kernel = Kernel.CreateKernelInNewThread();
		this.troySoarAgent = kernel.CreateAgent(name);
		
		this.inputLink = troySoarAgent.GetInputLink();
		this.outputLink = troySoarAgent.GetOutputLink();
		this.interactionLink = this.inputLink.CreateIdWME("interaction");
		
		if (Config.getProperty(dmName + ".config.runType", null).equals("debug"))
			troySoarAgent.SpawnDebugger(kernel.GetListenerPort());
		
		String absoluteSoarRulesPath = Config.getProperty(dmName+".soarRules",null);
		troySoarAgent.LoadProductions(absoluteSoarRulesPath);
		troySoarAgent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
	}

	@NonNull
	public List<Result> apply(LogicalForm call, Plan plan, DialogState state) {
		List <Result> resultList = new ArrayList<Result>();
		if (call.op.equals("writeToSoar")){
			messageToWrite = call;
			dialogStateToCallback = state;
			callToProcess = call;
		}
		if (call.op.equals("readFromSoar")){
			LogicalForm da  = readFromSoar();
			if (da == null) return resultList;
			Result result = new Result();
            result.newCall = call.clone();
            result.newCall.setArg(0, da);
            resultList.add(result);
		}
		else{
			new IllegalArgumentException("Unknown call: " + call.toString());
		}
		return resultList;
	}
	
	public void start(){
		if(isRunning){
			return;
		}
    	class AgentThread implements Runnable{
    		public void run(){
    	    	isRunning = true;
    			sendCommand("run");
    			isRunning = false;
    		}
    	}
    	Thread agentThread = new Thread(new AgentThread());
    	agentThread.start();
	}
	
	public void stop(){
		queueStop = true;
	}
	
	public String sendCommand(String command){
		return troySoarAgent.ExecuteCommandLine(command);
	}
	
	public void runEventHandler(int eventID, Object data, Agent agent, int phase) {
		stopAgentIfRequested();
		triggerOttoCallback();
		writeInputToSoar();
	}

	private void stopAgentIfRequested() {
		if(queueStop){
			sendCommand("stop");
			queueStop = false;
		}
	}
	
	private void writeInputToSoar() {
		if (messageToWrite != null) {
			if(messageToWrite.getArg(0).toString().equals("ActionCommand"))
				writeActionCommand(messageToWrite.getArgList());
			messageToWrite = null;
		}
	}
	
	private void writeActionCommand(List<LogicalForm> argList) {
		Identifier commandId = this.interactionLink.CreateIdWME("message");
		commandId.CreateStringWME("type", "action-command");
		Iterator<LogicalForm> argListIterator = argList.iterator();
		argListIterator.next();
		commandId.CreateStringWME("verb", argListIterator.next().toString());
		String baseObjectString = "object";
		while(argListIterator.hasNext()){
			commandId.CreateStringWME(baseObjectString, argListIterator.next().toString());
		}
		this.troySoarAgent.Commit();
	}
	
	private void triggerOttoCallback(){
		if(this.dialogStateToCallback != null && this.troySoarAgent.GetOutputLink() != null && this.troySoarAgent.GetOutputLink().GetNumberChildren() > 0){
			DispatchCallback callback = this.dialogStateToCallback.getDispatchCallback();
            callback.setUserTimeout("readOutputFromSoar", 0, REQUEST(new LogicalForm("readOutputFromSoar")), this.dialogStateToCallback); 
            this.dialogStateToCallback = null;
		}
	}

	private LogicalForm readFromSoar(){
		LogicalForm responseDialogAct = new LogicalForm();
		for (int i = 0; i < this.troySoarAgent.GetOutputLink().GetNumberChildren(); i++){
			Identifier messageId = this.troySoarAgent.GetOutputLink().GetChild(i).ConvertToIdentifier();
			if (messageId.GetAttribute().equals("message")){
				String dialog_act = messageId.GetParameterValue("dialog-act");
				String content = messageId.GetParameterValue("content");
				if(dialog_act.equals("inform")){
				responseDialogAct = INFORM(new LogicalForm(content));
				}
			}
		}
		return responseDialogAct;
	}
	
	
	public String getErrorMessage(LogicalForm call) {
		return null;
	}
	

}
