package com.parc.troy.interaction;

import java.util.Iterator;
import java.util.List;

import sml.Identifier;

import com.parc.troy.SoarHelper;
import com.parc.xi.dm.LogicalForm;

public class InteractionInputWriter {
	
	private Identifier interactionLink;
	
	public InteractionInputWriter(Identifier interactionLink){
		this.interactionLink = interactionLink;
	}
	
	public void writeMessage(LogicalForm messageToWrite) {
		//System.out.println(messageToWrite);
		clearInteractionLink();
		Identifier commandId = this.interactionLink.CreateIdWME("message");
		if(messageToWrite.getArg(0).toString().equals("ActionCommand")){
			commandId.CreateStringWME("type", "action-command");
			this.writeActionCommand(messageToWrite, commandId);
		}
	}
	
	private void clearInteractionLink() {
		SoarHelper.deleteAllChildren(this.interactionLink);
	}

	private void writeActionCommand(LogicalForm messageToWrite, Identifier commandId) {
		Iterator<LogicalForm> argListIterator = messageToWrite.getArgList().iterator();
		argListIterator.next();
		commandId.CreateStringWME("verb", argListIterator.next().toString());
		while(argListIterator.hasNext()){
			LogicalForm lf = argListIterator.next();
			if(lf.op != null & lf.op.equals("np")){
				//System.out.println("writing noun-phrase");
				Identifier npId = commandId.CreateIdWME("noun-phrase");
				writeNounPhrase(lf, npId);
			}
		}
	}
	
	private void writeNounPhrase(LogicalForm nounPhrase, Identifier id){
		//System.out.println("Noun phrase id is " + id.toString());
		Iterator<LogicalForm> argListIterator = nounPhrase.getArgList().iterator();
		while(argListIterator.hasNext()){
			LogicalForm lf = argListIterator.next();
			if(lf.op != null && lf.op.equals("dt")){
				writeDeterminer(lf.getArg(0), id);
			}
			if(lf.op != null && lf.op.equals("n")){
				writeNoun(lf.getArg(0), id);
			}
		}
	}
	
	private void writeDeterminer(LogicalForm determiner, Identifier id){
		//System.out.println("writing determiner " + determiner.op +  " on " + id.toString());
		id.CreateStringWME("determiner", determiner.op);
	}
	
	private void writeNoun(LogicalForm noun, Identifier id){
		//System.out.println("writing noun " + noun.op + " on " + id.toString());
		id.CreateStringWME("noun", noun.op);
	}
	
}
