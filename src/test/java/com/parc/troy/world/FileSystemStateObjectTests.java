/**
 * 
 */
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

/**
 * @author smohan
 *
 */
public class FileSystemStateObjectTests {
	
	private FileSystemStateObject fsObject;
	private String homeFolderPath;
	private Identifier worldId;
	private Agent testAgent;
	private Kernel testKernel;
	
	private File homeFolder;
	private File subFolder;
	private File testFile1;
	private File testFile2;
	private File subTestFile1;

	
	@Before
	public void setUp() throws Exception {
		testKernel = Kernel.CreateKernelInCurrentThread();
		testAgent = testKernel.CreateAgent("test-agent");
		worldId = testAgent.GetInputLink().CreateIdWME("world");
		
		fsObject = new FileSystemStateObject("DM.troy");
		homeFolderPath = FileSystemStateObjectTests.class.getResource("").getPath()+"/testHome";
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
	}

	
	@After
	public void tearDown() throws Exception {
		testKernel.delete();
		FileUtils.deleteDirectory(homeFolder);
	}

	@Test
	public void testUpdateWorldCreatedNewFile() throws IOException {
		
		
		File file = new File(homeFolderPath + "/test_generated_test_file.txt");
		file.createNewFile();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		Boolean foundFile = false;
		
		for(File ifile: fsObject.getObjectSet()){
			if(ifile.getName().equals("test_generated_test_file.txt")){
				foundFile = true;
			}
		}
		assertTrue(foundFile);
		file.delete();
	}
	
	@Test
	public void testUpdateWorldDeleteFile() throws IOException {
		
		
		File file = new File(homeFolderPath + "/test_generated_test_file.txt");
		file.createNewFile();
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		
		file.delete();
		fsObject.update();
		Boolean foundFile = false;
		for(File ifile: fsObject.getObjectSet()){
			if(ifile.getName().equals("test_generated_test_file.txt")){
				foundFile = true;
			}
		}
		assertFalse(foundFile);
	}
	
	@Test
	public void testWriteWorldInputPreviousPathisNull() throws IOException{
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertEquals(fsObject.getPreviousPath(), fsObject.getCurrentPath());
		assertNull(worldId.GetChild(2));
	}
	
	@Test
	public void testWriteWorldInputPreviousPathIsDifferent() throws IOException{
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		String subFolderPath = homeFolderPath+"/testSubFolder";
		subFolder = new File(subFolderPath);
		subFolder.mkdir();
		subTestFile1 = new File(homeFolderPath+"/testSubFolder/test_generated_subFile1.txt");
		subTestFile1.createNewFile();
		
		fsObject.setCurrentPath(subFolderPath);
		fsObject.setPreviousPath(homeFolderPath);
		fsObject.setFolder(subFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testSubFolder");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_subFile1.txt");
		assertEquals(fsObject.getPreviousPath(), fsObject.getCurrentPath());
		assertNull(worldId.GetChild(2));
	}
	
	@Test
	public void testWriteWorldInputFileAdded() throws IOException{
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 2);
		
		testFile2 = new File (homeFolderPath+"/test_generated_file2.txt");
		testFile2.createNewFile();
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 3);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertNotNull(worldId.GetChild(2).ConvertToIdentifier());
		assertEquals(worldId.GetChild(2).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(2).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file2.txt");
		assertEquals(fsObject.getPreviousPath(), fsObject.getCurrentPath());
		assertNull(worldId.GetChild(3));
	}
	
	@Test
	public void testWriteWorldInputFileDeleted() throws IOException{
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		testFile2 = new File (homeFolderPath+"/test_generated_file2.txt");
		testFile2.createNewFile();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 3);
		
		testFile2.delete();
		
		fsObject.update();
		fsObject.writeToSoar(worldId);
		
		assertEquals(worldId.GetNumberChildren(), 2);
		assertEquals(worldId.GetParameterValue("current-folder"), "testHome");
		assertNotNull(worldId.GetChild(1).ConvertToIdentifier());
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("type"),"file_object");
		assertEquals(worldId.GetChild(1).ConvertToIdentifier().GetParameterValue("name"),"test_generated_file1.txt");
		assertEquals(fsObject.getPreviousPath(), fsObject.getCurrentPath());
		assertNull(worldId.GetChild(2));
	}
	
	@Test
	public void testChangeDirectory(){
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		subFolder.mkdir();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "change-directory");
		Identifier directoryId = commandId.CreateIdWME("directory");
		directoryId.CreateStringWME("name", "testSubFolder");
		
		String testString = fsObject.getCurrentPath()+"/"+"testSubFolder";
		fsObject.readSoarCommandAndApply(commandId, null);
		assertEquals(fsObject.getCurrentPath(), testString);
	}
	
	@Test
	public void testCreateDirectory(){
		homeFolder = new File(homeFolderPath);
		homeFolder.mkdir();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "create-directory");
		Identifier directoryId = commandId.CreateIdWME("directory");
		directoryId.CreateStringWME("name", "testSubFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		
		assertNotNull(subFolder);	
	}
	

}
