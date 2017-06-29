package com.parc.troy.interaction;

import java.util.Iterator;

import org.apache.log4j.Logger;

import sml.Identifier;

import com.parc.troy.SoarHelper;
import com.parc.xi.dm.LogicalForm;



public class InteractionInputWriter {
	
	private static Logger LOGGER = Logger.getLogger(InteractionInputWriter.class.getName());
	
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
			Identifier lexId = commandId.CreateIdWME("lexical");
			this.writeActionCommand(messageToWrite, lexId);
		}
	}
	
	private void clearInteractionLink() {
		SoarHelper.deleteAllChildren(this.interactionLink);
	}

	private void writeActionCommand(LogicalForm messageToWrite, Identifier commandId) {
		LOGGER.debug("Messaged received from Otto is: " + messageToWrite.toString());
		Iterator<LogicalForm> argListIterator = messageToWrite.getArgList().iterator();
		argListIterator.next();
		while(argListIterator.hasNext()){
			LogicalForm lf = argListIterator.next();
			if(lf.op != null){
				if(lf.op.equals("verb")){
					writeVerb(lf, commandId);
				}
				if(lf.op.equals("entity")){
					Identifier entityId = commandId.CreateIdWME("entity");
					writeEntity(lf, entityId);
				}
				if(lf.op.equals("action-object")){
					Identifier id = commandId.CreateIdWME("action-object");
					writeActionObject(lf, id);
				}
			}
		}	
	}
	
	private void writeActionObject(LogicalForm actionObject, Identifier id) {
		LOGGER.debug("Writing action object: " + actionObject);
		Iterator<LogicalForm> argListIterator = actionObject.getArgList().iterator();
		while(argListIterator.hasNext()) {
			LogicalForm lf = argListIterator.next();
			if(lf.op.equals("entity")) {
				Identifier entityId = id.CreateIdWME("entity");
				writeEntity(lf, entityId);
			}
			if(lf.op.equals("action-specifier")) {
				writeActionSpecifier(lf, id);
			}
		}
	}
	
	private void writeVerb(LogicalForm verb, Identifier id){
		
		id.CreateStringWME("verb", verb.getArg(0).toString());
	}
	
	
	
	private void writeEntity(LogicalForm entity, Identifier id){
		LOGGER.debug("Writing entity: " + entity);
		Iterator<LogicalForm> argListIterator = entity.getArgList().iterator();
		while(argListIterator.hasNext()){
			LogicalForm lf = argListIterator.next();
			if(lf.op != null){
				if(lf.op.equals("type")){
					writeEntityType(lf, id);
				}
				if(lf.op.equals("has-set-property")){
					writeSetProperty(lf, id);
				}
				if(lf.op.equals("has-property")){
					writeProperty(lf, id);
				}
				if(lf.op.equals("name")){
					writeEntityName(lf,id);
				}
			}	
		}
	}
	
	private void writeEntityName(LogicalForm name, Identifier id){
		LOGGER.debug("Writing type: " + name);
		id.CreateStringWME("name", name.getArg(0).toString());
	}
	
	private void writeEntityType(LogicalForm type, Identifier id){
		LOGGER.debug("Writing type: " + type);
		id.CreateStringWME("type", type.getArg(0).toString());
	}
	
	private void writeSetProperty(LogicalForm setProperty, Identifier id){
		LOGGER.debug("Writing set-property: " + setProperty);
		Identifier setPropertyId = id.CreateIdWME("set-property");
		setPropertyId.CreateStringWME(setProperty.getArg(0).toString(), setProperty.getArg(1).toString());
	}
	
	private void writeProperty(LogicalForm property, Identifier id){
		LOGGER.debug("Writing property: " + property);
		Identifier propertyId = id.CreateIdWME("property");
		propertyId.CreateStringWME(property.getArg(0).toString(), property.getArg(1).toString());
	}
	
	private void writeActionSpecifier(LogicalForm specifier, Identifier id) {
		LOGGER.debug("Writing action specifier: " + specifier);
		id.CreateStringWME("specifier", specifier.getArg(0).toString());
	}
	
}
