package com.parc.troy.world;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sml.Agent;
import sml.Identifier;
import sml.Kernel;

public class WorldOutputReaderTests {
	private Kernel testKernel;
	private Agent testAgent;
	private World testWorld;
	private String homeFolderPath;
	private File homeFolder;
	private WorldOutputReader worldOR;
	
	@Before
	public void setUp() {
		testKernel = Kernel.CreateKernelInCurrentThread();
		testAgent = testKernel.CreateAgent("test-agent");
		testWorld = new World("DM.troy");
	}
	
	@After
	public void tearDown() throws IOException {
		testKernel.delete();
		FileUtils.deleteDirectory(homeFolder);
	}
	
	@Test
	public void testChangeDirectory(){
		homeFolderPath = WorldTest.class.getResource("").getPath()+"testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		subFolder.mkdir();
		
		testWorld.setCurrentPath(homeFolderPath);
		testWorld.setPreviousPath(null);
		testWorld.setFolder(homeFolder);
		testWorld.setObjectSet(new HashSet<File>(Arrays.asList(testWorld.getFolder().listFiles())));
		
		worldOR = new WorldOutputReader(testWorld, null);
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "change-directory");
		Identifier directoryId = commandId.CreateIdWME("directory");
		directoryId.CreateStringWME("name", "testSubFolder");
		
		String testString = testWorld.getCurrentPath()+"/"+"testSubFolder";
		worldOR.applySoarCommand(commandId);
		assertEquals(testWorld.getCurrentPath(), testString);
	}
	
	@Test
	public void testCreateDirectory(){
		homeFolderPath = WorldTest.class.getResource("").getPath()+"testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		
		testWorld.setCurrentPath(homeFolderPath);
		testWorld.setPreviousPath(null);
		testWorld.setFolder(homeFolder);
		testWorld.setObjectSet(new HashSet<File>(Arrays.asList(testWorld.getFolder().listFiles())));
		
		worldOR = new WorldOutputReader(testWorld, null);
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "create-directory");
		Identifier directoryId = commandId.CreateIdWME("directory");
		directoryId.CreateStringWME("name", "testSubFolder");
		
		worldOR.applySoarCommand(commandId);
		
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		
		assertNotNull(subFolder);
				
		
		
	}
	
	

}
