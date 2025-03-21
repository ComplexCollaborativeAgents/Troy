package com.parc.troy;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import com.parc.xi.dm.LogicalFormConstants;
import com.parc.xi.dm.agent.DialogRuleEngine;
import com.parc.xi.dm.agent.PlanStateAgent;
import com.parc.xi.dm.kb.KnowledgeBase;

public class TroyAgent extends PlanStateAgent{
	
	private SoarInterface soarInterface;
	private static Logger LOGGER = Logger.getLogger(TroyConsole.class.getName());
	
	public TroyAgent(String name, String dmName, KnowledgeBase kb) throws URISyntaxException, FileNotFoundException {
		
		this.name = LogicalFormConstants.AGENT;
		if (name == null || name.isEmpty()) name = "troy";
		if (dmName == null || dmName.isEmpty()) dmName = "troyDM";	
		this.soarInterface = new SoarInterface(name, dmName);
		
		this.configureAndStartDialogEngine(dmName, kb);
		this.soarInterface.start();
	}
	
	private void configureAndStartDialogEngine(String dmName, KnowledgeBase kb){
		engine = new DialogRuleEngine(name, dmName, kb);
		engine.addAction("writeToSoar", this.soarInterface);
		engine.addAction("readFromSoar", this.soarInterface);
		LOGGER.debug("Created new dialog engine.");
		addGenerator(engine);
	}
}
