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
	
	private LocalFileCollectionStateObject fsObject;
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
		
		fsObject = new LocalFileCollectionStateObject("DM.troy");
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
	public void testChangeFolder(){
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		subFolder.mkdir();
		
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "change-folder");
		Identifier directoryId = commandId.CreateIdWME("folder_object");
		directoryId.CreateStringWME("name", "testSubFolder");
		
		String testString = fsObject.getCurrentPath()+"/"+"testSubFolder";
		fsObject.readSoarCommandAndApply(commandId, null);
		assertEquals(fsObject.getCurrentPath(), testString);
	}
	
	@Test
	public void testCreateFolder(){
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "create-folder");
		Identifier folderId = commandId.CreateIdWME("folder_object");
		folderId.CreateStringWME("name", "testSubFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		String subFolderPath = homeFolderPath+"/testSubFolder";
		File subFolder = new File(subFolderPath);
		
		assertTrue(subFolder.isDirectory());	
	}
	
	@Test
	public void testCopyFile() throws IOException{
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "copy");
		Identifier fileId = commandId.CreateIdWME("file_object");
		fileId.CreateStringWME("name", "test_generated_file1.txt");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertEquals(fsObject.getPathToCopy(), homeFolderPath+"/test_generated_file1.txt");
		assertEquals(fsObject.getObjectNameToCopy(), "test_generated_file1.txt");
		assertEquals(fsObject.getObjectTypeToCopy(), "file_object");
		assertFalse(fsObject.getShouldDeleteOldPath());
		
		
	}
	
	@Test
	public void testCopyFolder() throws IOException{
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "copy");
		Identifier fileId = commandId.CreateIdWME("folder_object");
		fileId.CreateStringWME("name", "subTestFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertEquals(fsObject.getPathToCopy(), homeFolderPath+"/subTestFolder");
		assertEquals(fsObject.getObjectNameToCopy(), "subTestFolder");
		assertEquals(fsObject.getObjectTypeToCopy(), "folder_object");
		assertFalse(fsObject.getShouldDeleteOldPath());
		
	}
	
	@Test
	public void testCutFile() throws IOException{
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "cut");
		Identifier fileId = commandId.CreateIdWME("file_object");
		fileId.CreateStringWME("name", "test_generated_file1.txt");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertEquals(fsObject.getPathToCopy(), homeFolderPath+"/test_generated_file1.txt");
		assertEquals(fsObject.getObjectNameToCopy(), "test_generated_file1.txt");
		assertEquals(fsObject.getObjectTypeToCopy(), "file_object");
		assertTrue(fsObject.getShouldDeleteOldPath());
		
		
	}
	
	@Test
	public void testCutFolder() throws IOException{
		fsObject.setCurrentPath(homeFolderPath);
		fsObject.setPreviousPath(null);
		fsObject.setFolder(homeFolder);
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "cut");
		Identifier fileId = commandId.CreateIdWME("folder_object");
		fileId.CreateStringWME("name", "subTestFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertEquals(fsObject.getPathToCopy(), homeFolderPath+"/subTestFolder");
		assertEquals(fsObject.getObjectNameToCopy(), "subTestFolder");
		assertEquals(fsObject.getObjectTypeToCopy(), "folder_object");
		assertTrue(fsObject.getShouldDeleteOldPath());
	}
	
	@Test
	public void testPasteCopiedFile() throws IOException{
		fsObject.setPreviousPath(homeFolderPath);
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		fsObject.setCurrentPath(homeFolderPath+"/subTestFolder");
		fsObject.setFolder(testFolder1);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.setPathToCopy(homeFolderPath+"/test_generated_file1.txt");
		fsObject.setObjectNameToCopy("test_generated_file1.txt");
		fsObject.setObjectTypeToCopy("file");
		fsObject.setShouldDeleteOldPath(false);
		
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "paste");
		Identifier fileId = commandId.CreateIdWME("file_object");
		fileId.CreateStringWME("name", "test_generated_file1.txt");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertTrue(new File(homeFolderPath+"/subTestFolder/test_generated_file1.txt").isFile());
		assertTrue(testFile1.exists());
		assert(fsObject.getPathToCopy() == null);
		assert(fsObject.getObjectNameToCopy() == null);
		assert(fsObject.getObjectTypeToCopy() == null);
		assertFalse(fsObject.getShouldDeleteOldPath());
		
	}
	
	@Test
	public void testPasteCutFile() throws IOException{
		fsObject.setPreviousPath(homeFolderPath);
		testFile1 = new File(homeFolderPath+"/test_generated_file1.txt");
		testFile1.createNewFile();
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		fsObject.setCurrentPath(homeFolderPath+"/subTestFolder");
		fsObject.setFolder(testFolder1);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.setPathToCopy(homeFolderPath+"/test_generated_file1.txt");
		fsObject.setObjectNameToCopy("test_generated_file1.txt");
		fsObject.setObjectTypeToCopy("file");
		fsObject.setShouldDeleteOldPath(true);
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "paste");
		Identifier fileId = commandId.CreateIdWME("file_object");
		fileId.CreateStringWME("name", "test_generated_file1.txt");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertTrue(new File(homeFolderPath+"/subTestFolder/test_generated_file1.txt").isFile());
		assertFalse(testFile1.exists());
		assert(fsObject.getPathToCopy() == null);
		assert(fsObject.getObjectNameToCopy() == null);
		assert(fsObject.getObjectTypeToCopy() == null);
		assertFalse(fsObject.getShouldDeleteOldPath());
		
	}
	
	@Test
	public void testPasteCopiedFolder() throws IOException{
		fsObject.setPreviousPath(homeFolderPath);
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		File testFolder2 = new File(homeFolderPath+"/subTestFolder2");
		testFolder2.mkdir();
		new File(homeFolderPath+"/subTestFolder/subTestFile1.txt").createNewFile();
		new File(homeFolderPath+"/subTestFolder/subTestFile2.txt").createNewFile();
		fsObject.setCurrentPath(homeFolderPath+"/subTestFolder2");
		fsObject.setFolder(testFolder2);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.setPathToCopy(homeFolderPath+"/subTestFolder");
		fsObject.setObjectNameToCopy("subTestFolder");
		fsObject.setObjectTypeToCopy("folder");
		fsObject.setShouldDeleteOldPath(false);
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "paste");
		Identifier fileId = commandId.CreateIdWME("folder_object");
		fileId.CreateStringWME("name", "subTestFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder").isDirectory());
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder/subTestFile1.txt").isFile());
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder/subTestFile2.txt").isFile());
		assertTrue(testFolder1.exists());
		assert(fsObject.getPathToCopy() == null);
		assert(fsObject.getObjectNameToCopy() == null);
		assert(fsObject.getObjectTypeToCopy() == null);
		assertFalse(fsObject.getShouldDeleteOldPath());
		
	}
	@Test
	public void testPasteCutFolder() throws IOException{
		fsObject.setPreviousPath(homeFolderPath);
		File testFolder1 = new File(homeFolderPath+"/subTestFolder");
		testFolder1.mkdir();
		File testFolder2 = new File(homeFolderPath+"/subTestFolder2");
		testFolder2.mkdir();
		new File(homeFolderPath+"/subTestFolder/subTestFile1.txt").createNewFile();
		new File(homeFolderPath+"/subTestFolder/subTestFile2.txt").createNewFile();
		fsObject.setCurrentPath(homeFolderPath+"/subTestFolder2");
		fsObject.setFolder(testFolder2);
		fsObject.setObjectSet(new HashSet<File>(Arrays.asList(fsObject.getFolder().listFiles())));
		
		fsObject.setPathToCopy(homeFolderPath+"/subTestFolder");
		fsObject.setObjectNameToCopy("subTestFolder");
		fsObject.setObjectTypeToCopy("folder");
		fsObject.setShouldDeleteOldPath(true);
		
		Identifier commandId = testAgent.GetInputLink().CreateIdWME("command");
		commandId.CreateStringWME("name", "paste");
		Identifier fileId = commandId.CreateIdWME("folder_object");
		fileId.CreateStringWME("name", "subTestFolder");
		
		fsObject.readSoarCommandAndApply(commandId, null);
		
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder").isDirectory());
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder/subTestFile1.txt").isFile());
		assertTrue(new File(homeFolderPath+"/subTestFolder2/subTestFolder/subTestFile2.txt").isFile());
		assertFalse(testFolder1.exists());
		assert(fsObject.getPathToCopy() == null);
		assert(fsObject.getObjectNameToCopy() == null);
		assert(fsObject.getObjectTypeToCopy() == null);
		assertFalse(fsObject.getShouldDeleteOldPath());
		
	}

}
