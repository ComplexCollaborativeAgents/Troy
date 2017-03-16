package com.parc.troy;


import java.io.InputStream;
import java.net.URL;

import com.parc.xi.dm.Config;
import com.parc.xi.dm.DialogManager;
import com.parc.xi.dm.LogicalFormConstants;
import com.parc.xi.dm.agent.PlanStateAgent;
import com.parc.xi.dm.agent.DialogRuleEngine;
import com.parc.xi.dm.kb.KnowledgeBase;

import sml.Agent;
import sml.Kernel;
import sml.smlRunEventId;
import sml.Agent.RunEventInterface;
import sml.smlRhsEventId;

public class TroyAgent extends PlanStateAgent implements RunEventInterface{
	
	private Kernel kernel;
	private Agent troySoarAgent;
	private String troySource;
	
	
	public TroyAgent(String name, String dmName, KnowledgeBase kb){
		this.name = LogicalFormConstants.AGENT;
		if (name == null || name.isEmpty()) name = "troy";
		if (dmName == null || dmName.isEmpty()) dmName = "troyDM";
		
		this.configureAndStartSoarAgent(dmName);
		this.configureAndStartDialogEngine(dmName, kb);
	}
	
	void configureAndStartSoarAgent(String dmName)
	{
		this.kernel = Kernel.CreateKernelInNewThread();
		this.troySoarAgent = kernel.CreateAgent(this.name);
		if (Config.getProperty(dmName + ".config.runType", null).equals("debug"))
		{
			troySoarAgent.SpawnDebugger(kernel.GetListenerPort());
		}
		String absoluteSoarRulesPath = Config.getProperty(dmName+".soarRules",null);
		troySoarAgent.LoadProductions(absoluteSoarRulesPath);
		troySoarAgent.RegisterForRunEvent(smlRunEventId.smlEVENT_AFTER_INPUT_PHASE, this, null);
	}
	
	
	private void configureAndStartDialogEngine(String dmName, KnowledgeBase kb)
	{
		engine = new DialogRuleEngine(name, dmName, kb);
		engine.readRules(Config.getProperty(dmName+".dialogRules",null));
		addGenerator(engine);
	}

	public void runEventHandler(int arg0, Object arg1, Agent arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	
}
