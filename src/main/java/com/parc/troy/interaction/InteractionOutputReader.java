package com.parc.troy.interaction;

import static com.parc.xi.dm.LogicalFormConstants.BOOLAND;
import static com.parc.xi.dm.LogicalFormConstants.INFORM;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.parc.troy.SoarInterface;
import com.parc.xi.dm.LogicalForm;

import sml.Agent;
import sml.Identifier;

public class InteractionOutputReader {
	
	private Agent troySoarAgent;
	private SoarInterface soarI;
	
	private static Logger LOGGER = Logger.getLogger(InteractionOutputReader.class.getName());
	
	public InteractionOutputReader(Agent troySoarAgent, SoarInterface soarI){
		this.troySoarAgent = troySoarAgent;
		this.soarI = soarI;
	}
	
	public LogicalForm readSoarMessage(){
		LogicalForm responseDialogAct = new LogicalForm();
		for (int i = 0; i < this.troySoarAgent.GetOutputLink().GetNumberChildren(); i++){
			Identifier messageId = this.troySoarAgent.GetOutputLink().GetChild(i).ConvertToIdentifier();
			if (messageId.GetAttribute().equals("message")){
				String dialogAct = messageId.GetParameterValue("dialog-act");
				if(dialogAct.equals("inform")){
					Identifier contentId = messageId.GetChild(0).ConvertToIdentifier();
					responseDialogAct = INFORM(this.parseContent(contentId));
				}
			}
			if (this.soarI != null){
			 this.soarI.getIdentifiersToRemove().add(messageId);
			 LOGGER.debug("Number of identifiers to remove from soarAgent outputLink: " + this.soarI.getIdentifiersToRemove().size());
			}
		}
		return responseDialogAct;
	}
	
	private LogicalForm parseContent(Identifier contentId){
		List<LogicalForm> itemList = new ArrayList<LogicalForm>();
		int i = 0;
		for(i = 0 ;i < contentId.GetNumberChildren(); i++){
			itemList.add(new LogicalForm(contentId.GetChild(i).GetValueAsString()));
		}
		return BOOLAND(itemList);
	}
}
