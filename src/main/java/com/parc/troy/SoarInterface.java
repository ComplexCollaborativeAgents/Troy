package com.parc.troy;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;

import com.parc.xi.dm.Config;
import com.parc.xi.dm.LogicalForm;
import com.parc.xi.dm.agent.DialogRuleFn;
import com.parc.xi.dm.state.DialogState;
import com.parc.xi.dm.state.Plan;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;
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
		{
			troySoarAgent.SpawnDebugger(kernel.GetListenerPort());
		}
		String absoluteSoarRulesPath = Config.getProperty(dmName+".soarRules",null);
		troySoarAgent.LoadProductions(absoluteSoarRulesPath);
		troySoarAgent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
	}

	@NonNull
	public List<Result> apply(LogicalForm call, Plan plan, DialogState state) {
		System.out.println("I am here");
		List <Result> resultList = new ArrayList<Result>();
		if (call.op.equals("writeToSoar")){
			messageToWrite = call;
			System.out.println("Command to soar sent out.");
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
		System.out.println("In Soar's running loop");
		stopAgentIfRequested();
		readOutputFromSoar();
		writeInputToSoar();
	}

	private void stopAgentIfRequested() {
		if(queueStop){
			sendCommand("stop");
			queueStop = false;
		}
	}
	
	private void writeInputToSoar() {
		if (messageToWrite != null){
			System.out.println("Write message to Soar in the running loop");
			System.out.println(messageToWrite.getArg(0).toString());
			if(messageToWrite.getArg(0).toString().equals("ActionCommand"))
				writeActionCommand(messageToWrite.getArgList());
			messageToWrite = null;
		}
	}
	
	private void writeActionCommand(List<LogicalForm> argList) {
		Identifier commandId = this.interactionLink.CreateIdWME("action-command");
		Iterator<LogicalForm> argListIterator = argList.iterator();
		argListIterator.next();
		commandId.CreateStringWME("verb", argListIterator.next().toString());
		System.out.println("Wrote verb");
		String baseObjectString = "object";
		Integer i = 0;
		while(argListIterator.hasNext()){
			commandId.CreateStringWME(baseObjectString.concat(i.toString()), argListIterator.next().toString());
			i++;
			System.out.println("Wrote object.");
		}
		this.troySoarAgent.Commit();
	}

	private void readOutputFromSoar(){
		
	}
	
	
	public String getErrorMessage(LogicalForm call) {
		return null;
	}
	

}
