package com.parc.troy;

import static com.parc.xi.dm.LogicalFormConstants.INFORM;
import static com.parc.xi.dm.LogicalFormConstants.REQUEST;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.Kernel;
import sml.smlRunEventId;

import com.parc.xi.dm.Config;
import com.parc.xi.dm.DispatchCallback;
import com.parc.xi.dm.LogicalForm;
import com.parc.xi.dm.agent.DialogRuleFn;
import com.parc.xi.dm.state.DialogState;
import com.parc.xi.dm.state.Plan;


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

	public SoarInterface(String name, String dmName) throws URISyntaxException {
        this.configureAndStartSoarAgent(name, dmName);
	}
	
	void configureAndStartSoarAgent(String name, String dmName) throws URISyntaxException
	{
		this.kernel = Kernel.CreateKernelInNewThread();
		this.troySoarAgent = kernel.CreateAgent(name);
		
		this.inputLink = troySoarAgent.GetInputLink();
		this.setOutputLink(troySoarAgent.GetOutputLink());
		this.interactionLink = this.inputLink.CreateIdWME("interaction");
		
		if (Config.getProperty(dmName + ".config.runType", null).equals("debug"))
			troySoarAgent.SpawnDebugger(kernel.GetListenerPort());
		
		String absoluteSoarRulesPath = getFullPath(Config.getProperty(dmName+".soarRules",null));
		System.out.println("Loading files from " + absoluteSoarRulesPath);
		troySoarAgent.LoadProductions(absoluteSoarRulesPath);
		troySoarAgent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
	}

	@NonNull
	public List<Result> apply(LogicalForm call, Plan plan, DialogState state) {
		List <Result> resultList = new ArrayList<Result>();
		if (call.op.equals("writeToSoar")){
			messageToWrite = call;
			dialogStateToCallback = state;
			setCallToProcess(call);
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

	/**
	 * Get the filename path for the specified resource value. Resource values might contain a classpath
	 * prefix indicating they should be found by searching the current classpath. Whether the value is
	 * specified as a classpath or absolute path, this method returns the absolute path name to the resource.
	 * @param resourceValue the value specified as a property value that might have the classpath prefix
	 * @return the full path to the specified resource
	 * @throws URISyntaxException if the file path is invalid or can't be read
	 */
	protected String getFullPath(String resourceValue) throws URISyntaxException {
	    final String CP_PREFIX = "classpath:";
	    String fullPathString = null;
        File soarRulesFile = null;

        if (resourceValue.startsWith(CP_PREFIX)) {
            URL resourcePath = getClass().getResource(resourceValue.substring(CP_PREFIX.length()));
            if (resourcePath == null) throw new URISyntaxException(resourceValue, "Invalid path to specified resource file");
            soarRulesFile = Paths.get(resourcePath.toURI()).toFile();
            resourceValue = soarRulesFile.getAbsoluteFile().toString();
        } else {
            soarRulesFile = Paths.get(resourceValue).toFile();
        }
        if (! soarRulesFile.canRead()) throw new URISyntaxException(resourceValue, "Invalid path to specified resource file");
        fullPathString = soarRulesFile.toString();
        return fullPathString;
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
				String dialogAct = messageId.GetParameterValue("dialog-act");
				String content = messageId.GetParameterValue("content");
				if(dialogAct.equals("inform")){
				responseDialogAct = INFORM(new LogicalForm(content));
				}
			}
		}
		return responseDialogAct;
	}
	
	
	public String getErrorMessage(LogicalForm call) {
		return null;
	}

    public String getTroySource() {
        return troySource;
    }

    public void setTroySource(String troySource) {
        this.troySource = troySource;
    }

    public Identifier getOutputLink() {
        return outputLink;
    }

    public void setOutputLink(Identifier outputLink) {
        this.outputLink = outputLink;
    }

    public LogicalForm getCallToProcess() {
        return callToProcess;
    }

    public void setCallToProcess(LogicalForm callToProcess) {
        this.callToProcess = callToProcess;
    }
	

}
