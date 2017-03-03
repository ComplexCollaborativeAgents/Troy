package com.parc.troy;

import com.parc.xi.dm.LogicalFormConstants;
import com.parc.xi.dm.kb.KnowledgeBase;
import com.parc.xi.dm.agent.PlanStateAgent;
import com.parc.xi.dm.qa.QuestionAnswerer;
import com.parc.xi.dm.agent.DialogRuleEngine;


public class TroyAgent extends PlanStateAgent{
	
	QuestionAnswerer qa;
	KnowledgeBase kb;
	
	public TroyAgent(String name, String dmName, KnowledgeBase kb){
		this.name = LogicalFormConstants.AGENT; // SM: what is this?
		if (name == null || name.isEmpty()) name = "TroyAgent";
		if (dmName == null || dmName.isEmpty()) dmName = "Troy";
		this.kb = kb;
		
		engine = new DialogRuleEngine(name, dmName, kb);
		engine.readRules("classpath:/dialog-rules.txt");
		addGenerator(engine);
	}

}
