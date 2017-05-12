package com.parc.troy.world;

import java.io.File;

import org.apache.log4j.Logger;

import com.parc.troy.SoarInterface;

import sml.Identifier;

public class WorldOutputReader {
	
	private World world;
	private SoarInterface soarI;
	private static Logger LOGGER = Logger.getLogger(WorldOutputReader.class.getName());
	
	public WorldOutputReader (World world, SoarInterface soarI){
		this.world = world;
		this.soarI = soarI;
	}
	
	public void applySoarCommand(Identifier commandId){
		String command = commandId.GetParameterValue("name");
		if(command.equals("change-directory")){
			for(int i = 0; i < commandId.GetNumberChildren(); i++) {
				if(commandId.GetChild(i).GetAttribute().equals("directory")){
					String directory = commandId.GetChild(i).ConvertToIdentifier().GetParameterValue("name");
					this.processChangeDirectory(directory);
				}
			}
		}
		
		if(command.equals("create-directory")){
			for(int i = 0; i < commandId.GetNumberChildren(); i++) {
				if(commandId.GetChild(i).GetAttribute().equals("directory")){
					String directoryName = commandId.GetChild(i).ConvertToIdentifier().GetParameterValue("name");
					this.processCreateDirectory(directoryName);
				}
			}
		}
		
		if(this.soarI != null){
			this.soarI.getIdentifiersToRemove().add(commandId);
		}
	}
	
	private void processCreateDirectory(String directoryName){
		String currentPath = this.world.getCurrentPath();
		String folderPath = currentPath+"/" + directoryName;
		File newFolder = new File(folderPath);
		newFolder.mkdir();
	}
	
	private void processChangeDirectory(String directory){
		String currentPath = this.world.getCurrentPath();
		String newCurrentPath = currentPath + "/" + directory;
		this.world.setCurrentPath(newCurrentPath);
		LOGGER.debug("Changed to directory " + this.world.getCurrentPath());
	}
}
