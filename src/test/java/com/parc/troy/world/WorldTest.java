package com.parc.troy.world;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class WorldTest {
	
	private World world;
	private String homeFolderPath;
	
	@Before
	public void setUp() throws Exception {
		world = new World("DM.troy");
		homeFolderPath = WorldTest.class.getResource("").getPath();
		world.setCurrentPath(homeFolderPath);
		world.setPreviousPath(homeFolderPath);
		world.setFolder(new File(homeFolderPath));
		world.setObjectSet(new HashSet<File>(Arrays.asList(world.getFolder().listFiles())));
	}

	@Test
	public void testUpdateWorldCreatedNewFile() throws IOException {
		File file = new File(homeFolderPath + "test_generated_test_file.txt");
		file.createNewFile();
		world.updateWorld();
		Boolean foundFile = false;
		
		for(File ifile: world.getObjectSet()){
			if(ifile.getName().equals("test_generated_test_file.txt")){
				foundFile = true;
			}
		}
		assertTrue(foundFile);
		file.delete();
	}
	
	@Test
	public void testUpdateWorldDeleteFile() throws IOException {
		File file = new File(homeFolderPath + "test_generated_test_file.txt");
		file.createNewFile();
		world.updateWorld();
		file.delete();
		world.updateWorld();
		Boolean foundFile = false;
		for(File ifile: world.getObjectSet()){
			if(ifile.getName().equals("test_generated_test_file.txt")){
				foundFile = true;
			}
		}
		assertFalse(foundFile);
	}

}
