package com.parc.troy.world;

import com.parc.troy.SoarInterface;

import sml.Agent;
import sml.Identifier;

public class WorldOutputReader {
	
	private World world;
	private SoarInterface soarI;
	
	public WorldOutputReader (World world, SoarInterface soarI){
		this.world = world;
		this.soarI = soarI;
	}
	
	public void applySoarCommand(Identifier commandId){
		String command = commandId.GetParameterValue("name");
		if(command.equals("change-directory")){
			String directory = commandId.GetChild(0).ConvertToIdentifier().GetParameterValue("name");
			this.processChangeDirectory(directory);
			this.soarI.getIdentifiersToRemove().add(commandId);
		}
	}
	
	private void processChangeDirectory(String directory){
		String currentPath = this.world.getCurrentPath();
		String newCurrentPath = currentPath + "/" + directory;
		this.world.setCurrentPath(newCurrentPath);
		//System.out.println("New current directory " + this.world.getCurrentPath());
	}
}
