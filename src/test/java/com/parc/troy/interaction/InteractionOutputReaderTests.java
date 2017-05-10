package com.parc.troy.interaction;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.parc.troy.SoarInterface;
import com.parc.xi.dm.LogicalForm;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;

public class InteractionOutputReaderTests {
	private Kernel testKernel;
	private Agent testAgent;
	private InteractionOutputReader interactionOR;
	
	@Before
	public void setUp() {
		testKernel = Kernel.CreateKernelInCurrentThread();
		testAgent = testKernel.CreateAgent("test-agent");
		interactionOR = new InteractionOutputReader(testAgent, null);
	}
	
	@After
	public void tearDown() throws IOException {
		testKernel.delete();
	}
	
	@Test
	public void testParseInform(){
		Boolean success = testAgent.LoadProductions("./src/test/java/com/parc/troy/interaction/test.soar");
		assertTrue(success);
		testAgent.RunSelfTilOutput();
		assertEquals(this.testAgent.GetNumberCommands(), 1);
		LogicalForm response = interactionOR.readSoarMessage();
		String responseString = response.toString();
		assertEquals(responseString, "Inform(And(test-generated-file2,test-generated-file1))");
	}
}
