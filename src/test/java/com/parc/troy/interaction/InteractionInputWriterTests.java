package com.parc.troy.interaction;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.*;

import com.parc.xi.dm.LogicalForm;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;

public class InteractionInputWriterTests {
	
	
	private Kernel testKernel;
	private Agent testAgent;
	private Identifier interactionId;
	private InteractionInputWriter interactionIW;

	@Before
	public void setUp() {
		testKernel = Kernel.CreateKernelInCurrentThread();
		testAgent = testKernel.CreateAgent("test-agent");
		interactionId = testAgent.GetInputLink().CreateIdWME("world");
		interactionIW = new InteractionInputWriter(interactionId);
	}
	
	
	@After
	public void tearDown() throws IOException {
		testKernel.delete();
	}

	@Test
	public void testWriteActionCommandSingleArgument() {
		LogicalForm entity = new LogicalForm("entity", 
				new LogicalForm("has-set-property", new LogicalForm("determiner"), new LogicalForm ("distributive-all")), 
				new LogicalForm("type", new LogicalForm ("file")));
		LogicalForm message = new LogicalForm("writeToSoar", new LogicalForm("ActionCommand"), new LogicalForm("verb", new LogicalForm("list")), entity);
		interactionIW.writeMessage(message);
		assertEquals(interactionId.GetNumberChildren(), 1);
		assertEquals(interactionId.GetChild(0).GetAttribute(), "message");
		Identifier messageId = interactionId.GetChild(0).ConvertToIdentifier();
		assertEquals(messageId.GetNumberChildren(), 3);
		assertEquals(messageId.GetParameterValue("type"), "action-command");
		assertEquals(messageId.GetParameterValue("verb"), "list");
		Identifier entityId = messageId.GetChild(2).ConvertToIdentifier();
		assertEquals(entityId.GetAttribute(), "entity");
		assertEquals(entityId.GetParameterValue("type"), "file");
		Identifier setPropertyId = entityId.GetChild(0).ConvertToIdentifier();
		assertEquals(setPropertyId.GetParameterValue("determiner"), "distributive-all");
	}
	
	// writeToSoar(ActionCommand,verb(create),entity(type(folder),has-set-property(determiner,indefinite)),named-entity(proposals))
	@Test
	public void testWriteActionCommandTwoArguments(){
		LogicalForm entity = new LogicalForm("entity", 
				new LogicalForm("has-set-property", new LogicalForm("determiner"), new LogicalForm ("indefinite")), 
				new LogicalForm("type", new LogicalForm ("folder")));
		LogicalForm namedEntity = new LogicalForm("entity", new LogicalForm("name", new LogicalForm("proposals")));
		LogicalForm message = new LogicalForm("writeToSoar", 
				new LogicalForm("ActionCommand"), 
				new LogicalForm("verb", new LogicalForm("create")), 
				entity, namedEntity);
		interactionIW.writeMessage(message);
		assertEquals(interactionId.GetNumberChildren(), 1);
		assertEquals(interactionId.GetChild(0).GetAttribute(), "message");
		Identifier messageId = interactionId.GetChild(0).ConvertToIdentifier();
		assertEquals(messageId.GetNumberChildren(), 4);
		assertEquals(messageId.GetParameterValue("type"), "action-command");
		assertEquals(messageId.GetParameterValue("verb"), "create");
		Identifier entityId = messageId.GetChild(2).ConvertToIdentifier();
		assertEquals(entityId.GetAttribute(), "entity");
		assertEquals(entityId.GetParameterValue("type"), "folder");
		Identifier setPropertyId = entityId.GetChild(0).ConvertToIdentifier();
		assertEquals(setPropertyId.GetParameterValue("determiner"), "indefinite");
		Identifier namedEntityId = messageId.GetChild(3).ConvertToIdentifier();
		assertEquals(namedEntityId.GetAttribute(), "entity");
		assertEquals(namedEntityId.GetParameterValue("name"), "proposals");
		
		
	}
}
