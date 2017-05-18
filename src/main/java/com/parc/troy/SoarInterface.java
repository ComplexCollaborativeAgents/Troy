package com.parc.troy;

import static com.parc.xi.dm.LogicalFormConstants.REQUEST;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sml.Agent;
import sml.Agent.RunEventInterface;
import sml.Identifier;
import sml.Kernel;
import sml.smlRunEventId;

import com.parc.troy.interaction.InteractionInputWriter;
import com.parc.troy.interaction.InteractionOutputReader;
import com.parc.troy.world.World;
import com.parc.xi.dm.Config;
import com.parc.xi.dm.DispatchCallback;
import com.parc.xi.dm.LogicalForm;
import com.parc.xi.dm.agent.DialogRuleFn;
import com.parc.xi.dm.state.DialogState;
import com.parc.xi.dm.state.Plan;


public class SoarInterface implements DialogRuleFn, RunEventInterface {
	
	private static Logger LOGGER = Logger.getLogger(SoarInterface.class.getName());
	
	private Kernel kernel;
	private Agent troySoarAgent;
	private String troySource;
	private Identifier inputLink;
	private Identifier outputLink;
	private Identifier interactionLink;
	private Identifier worldLink;
	
	private InteractionInputWriter interactionIW;
	private InteractionOutputReader interactionOR;
	private List<Identifier> identifiersToRemove;
	private World world;
	
	private boolean isRunning = false;
	private boolean queueStop = false;
	
	private LogicalForm messageToWrite;
	private DialogState dialogStateToCallback;
	private LogicalForm callToProcess;

	public SoarInterface(String name, String dmName) throws URISyntaxException, FileNotFoundException {
        this.configureAndStartSoarAgent(name, dmName);
	}
	
	void configureAndStartSoarAgent(String name, String dmName) throws URISyntaxException, FileNotFoundException
	{
		this.kernel = Kernel.CreateKernelInNewThread();
		this.troySoarAgent = kernel.CreateAgent(name);
		this.inputLink = troySoarAgent.GetInputLink();
		this.setOutputLink(troySoarAgent.GetOutputLink());
		this.interactionLink = this.inputLink.CreateIdWME("interaction");
		this.setWorldLink(this.inputLink.CreateIdWME("world"));
		
		this.interactionIW = new InteractionInputWriter(this.interactionLink);
		this.setIdentifiersToRemove(new ArrayList<Identifier>());
		this.interactionOR = new InteractionOutputReader(this.troySoarAgent, this);
		this.world = new World(dmName, this, this.worldLink);
		
		if (Config.getProperty(dmName + ".config.runType", null).equals("debug"))
			troySoarAgent.SpawnDebugger(kernel.GetListenerPort());
		
		String absoluteSoarRulesPath = ConfigHelper.getFullFilePath(Config.getProperty(dmName+".soarRules",null));
		//System.out.println("Loading files from " + absoluteSoarRulesPath);
		troySoarAgent.LoadProductions(absoluteSoarRulesPath);
		troySoarAgent.RegisterForRunEvent(smlRunEventId.smlEVENT_BEFORE_INPUT_PHASE, this, null);
	}

	@NonNull
	public List<Result> apply(LogicalForm call, Plan plan, DialogState state) {
		List <Result> resultList = new ArrayList<Result>();
		if (call.op.equals("writeToSoar")){
			LOGGER.debug("Message from Otto: " + call);
			messageToWrite = call;
			dialogStateToCallback = state;
			setCallToProcess(call);
		}
		if (call.op.equals("readFromSoar")){
			LogicalForm da  = this.interactionOR.readSoarMessage();
			if (da == null) return resultList;
			Result result = new Result();
            result.newCall = call.clone();
            result.newCall.setArg(0, da);
            resultList.add(result);
		}
		else {
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
		try {
			Thread.sleep(4);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopAgentIfRequested();
		readOutputFromSoar();
		deleteOutputIdentifiers();
		this.world.update();
		writeInteractionInputToSoar();                                                                                                                   
		this.world.writeToSoar();
	}
	
	private void deleteOutputIdentifiers(){
//		if(this.identifiersToRemove.size() > 0){
//			System.out.println(this.identifiersToRemove.size() + " identifiers to delete");
//		}
		for(Identifier id: this.getIdentifiersToRemove()){
			id.AddStatusComplete();
		}
		this.identifiersToRemove.clear();
	}
	
	private void writeInteractionInputToSoar() {
		if (this.messageToWrite != null) {
			//System.out.println("in writeInputToSoar");
			this.interactionIW.writeMessage(this.messageToWrite);
			this.troySoarAgent.Commit();
			this.messageToWrite = null;
		}
	}


	private void stopAgentIfRequested() {
		if(queueStop){
			sendCommand("stop");
			queueStop = false;
		}
	}
	
	private void readOutputFromSoar(){
		Boolean shouldOttoCallback = false;
		if(this.dialogStateToCallback != null && this.troySoarAgent.GetOutputLink() != null && this.troySoarAgent.GetOutputLink().GetNumberChildren() > 0){
			for (int i = 0; i < this.troySoarAgent.GetOutputLink().GetNumberChildren(); i++) {
				Identifier outputId = this.troySoarAgent.GetOutputLink().GetChild(i).ConvertToIdentifier();
					if (outputId.GetAttribute().equals("message")){
			            shouldOttoCallback = true;
					}
					if (outputId.GetAttribute().equals("command")){
						this.world.readSoarOutputAndApply(outputId);
					}
				}
			
			if (shouldOttoCallback){
				DispatchCallback callback = this.dialogStateToCallback.getDispatchCallback();
				callback.setUserTimeout("readOutputFromSoar", 0, REQUEST(new LogicalForm("readOutputFromSoar")), this.dialogStateToCallback); 
				this.dialogStateToCallback = null;
			}
		}
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

	public InteractionInputWriter getInputWriter() {
		return interactionIW;
	}

	public void setInputWriter(InteractionInputWriter inputWriter) {
		this.interactionIW = inputWriter;
	}

	public Identifier getWorldLink() {
		return worldLink;
	}

	public void setWorldLink(Identifier worldLink) {
		this.worldLink = worldLink;
	}

	public InteractionOutputReader getInteractionOR() {
		return interactionOR;
	}

	public void setInteractionOR(InteractionOutputReader interactionOR) {
		this.interactionOR = interactionOR;
	}

	public List<Identifier> getIdentifiersToRemove() {
		return identifiersToRemove;
	}

	public void setIdentifiersToRemove(List<Identifier> identifiersToRemove) {
		this.identifiersToRemove = identifiersToRemove;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
	

}
