package com.parc.troy.interaction;

import static com.parc.xi.dm.LogicalFormConstants.INFORM;
import static com.parc.xi.dm.LogicalFormConstants.BOOLAND;

import java.util.ArrayList;
import java.util.List;

import com.parc.xi.dm.LogicalForm;

import sml.Agent;
import sml.Identifier;

public class InteractionOutputReader {
	
	private Agent troySoarAgent;
	
	public InteractionOutputReader(Agent troySoarAgent){
		this.troySoarAgent = troySoarAgent;
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
