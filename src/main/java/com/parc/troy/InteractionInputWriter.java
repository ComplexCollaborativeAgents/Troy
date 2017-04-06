package com.parc.troy;

import java.util.Iterator;
import java.util.List;

import sml.Identifier;

import com.parc.xi.dm.LogicalForm;

public class InteractionInputWriter {
	void writeMessage(LogicalForm messageToWrite, Identifier interactionLink) {
		//System.out.println("writeMessage");
		Identifier commandId = interactionLink.CreateIdWME("message");
		if(messageToWrite.getArg(0).toString().equals("ActionCommand")){
			commandId.CreateStringWME("type", "action-command");
			this.writeActionCommand(messageToWrite, commandId);
		}
	}
	
	void writeActionCommand(LogicalForm messageToWrite, Identifier commandId) {
		Iterator<LogicalForm> argListIterator = messageToWrite.getArgList().iterator();
		argListIterator.next();
		commandId.CreateStringWME("verb", argListIterator.next().toString());
		String baseObjectString = "object";
		while(argListIterator.hasNext()){
			LogicalForm lf = argListIterator.next();
			if(lf.op != null & lf.op.equals("np")){
				//System.out.println("writing noun-phrase");
				Identifier npId = commandId.CreateIdWME("noun-phrase");
				writeNounPhrase(lf, npId);
			}
		}
	}
	
	void writeNounPhrase(LogicalForm nounPhrase, Identifier id){
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
	
	void writeDeterminer(LogicalForm determiner, Identifier id){
		//System.out.println("writing determiner " + determiner.op +  " on " + id.toString());
		id.CreateStringWME("determiner", determiner.op);
	}
	
	void writeNoun(LogicalForm noun, Identifier id){
		//System.out.println("writing noun " + noun.op + " on " + id.toString());
		id.CreateStringWME("noun", noun.op);
	}
	
}
