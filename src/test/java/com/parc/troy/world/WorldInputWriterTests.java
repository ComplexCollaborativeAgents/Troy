package com.parc.troy.world;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.*;

import sml.*;

public class WorldInputWriterTests {
	private World world;
	private WorldInputWriter worldIW;
	private String homeFolderPath;
	private Identifier worldId;
	private Agent testAgent;
	private Kernel testKernel;
	
	private File homeFolder;
	private File subFolder;
	private File testFile1;
	private File testFile2;
	private File subTestFile1;
	
	// 
	
	@Before
	public void setUp() {
		testKernel = Kernel.CreateKernelInCurrentThread();
		testAgent = testKernel.CreateAgent("test-agent");
		worldId = testAgent.GetInputLink().CreateIdWME("world");
		world = new World("DM.troy");
	}
	
	@After
	public void tearDown() throws IOException {
		testKernel.delete();
		FileUtils.deleteDirectory(homeFolder);
	}
	
	@Test
	public void testWriteWorldInputPreviousPathisNull() throws IOException{
		// setup files for testing
		homeFolderPath = WorldTest.class.getResource("").getPath()+"/testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		
		world.setCurrentPath(homeFolderPath);
		world.setPreviousPath(null);
		world.setFolder(homeFolder);
		world.setObjectSet(new HashSet<File>(Arrays.asList(world.getFolder().listFiles())));
		worldIW = new WorldInputWriter(worldId, world);
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertEquals(world.getPreviousPath(), world.getCurrentPath());
		assertNull(worldId.GetChild(2));
	}
	
	@Test
	public void testWriteWorldInputPreviousPathIsDifferent() throws IOException{
		// setup files for testing
		homeFolderPath = WorldTest.class.getResource("").getPath()+"/testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		String subFolderPath = homeFolderPath+"/testSubFolder";
		subFolder = new File(subFolderPath);
		subFolder.mkdir();
		subTestFile1 = new File(homeFolderPath+"/testSubFolder/test_generated_subFile1.txt");
		subTestFile1.createNewFile();
		
		world.setCurrentPath(subFolderPath);
		world.setPreviousPath(homeFolderPath);
		world.setFolder(subFolder);
		world.setObjectSet(new HashSet<File>(Arrays.asList(world.getFolder().listFiles())));
		worldIW = new WorldInputWriter(worldId, world);
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testSubFolder");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_subFile1.txt");
		assertEquals(world.getPreviousPath(), world.getCurrentPath());
		assertNull(worldId.GetChild(2));
	}
	
	@Test
	public void testWriteWorldInputFileAdded() throws IOException{
		homeFolderPath = WorldTest.class.getResource("").getPath()+"/testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		
		world.setCurrentPath(homeFolderPath);
		world.setPreviousPath(null);
		world.setFolder(homeFolder);
		
		world.setObjectSet(new HashSet<File>(Arrays.asList(world.getFolder().listFiles())));
		worldIW = new WorldInputWriter(worldId, world);
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 2);
		
		testFile2 = new File (homeFolderPath+"/test_generated_file2.txt");
		testFile2.createNewFile();
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 3);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertNotNull(worldId.GetChild(2).ConvertToIdentifier());
		assertEquals(worldId.GetChild(2).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(2).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file2.txt");
		assertEquals(world.getPreviousPath(), world.getCurrentPath());
		assertNull(worldId.GetChild(3));
	}
	
	@Test
	public void testWriteWorldInputFileDeleted() throws IOException{
		homeFolderPath = WorldTest.class.getResource("").getPath()+"/testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		testFile2 = new File (homeFolderPath+"/test_generated_file2.txt");
		testFile2.createNewFile();
		
		world.setCurrentPath(homeFolderPath);
		world.setPreviousPath(null);
		world.setFolder(homeFolder);
		
		world.setObjectSet(new HashSet<File>(Arrays.asList(world.getFolder().listFiles())));
		worldIW = new WorldInputWriter(worldId, world);
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 3);
		
		testFile2.delete();
		
		world.updateWorld();
		worldIW.writeWorldInput();
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertEquals(world.getPreviousPath(), world.getCurrentPath());
		assertNull(worldId.GetChild(2));
		
		
	}

}
