package com.parc.troy.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import sml.Identifier;
import sml.WMElement;

import com.parc.troy.ConfigHelper;
import com.parc.xi.dm.Config;

public class WorldInputWriter {
	
	private String currentPath;
	private String homePath;
	
	private Identifier worldId;

	public WorldInputWriter(String dmName, Identifier worldId) throws URISyntaxException, FileNotFoundException{
		System.out.println("DMname is " + dmName);
		this.homePath = Config.getProperty(dmName+".Home",null);;
		this.currentPath = this.homePath;
		this.worldId = worldId;
	}
	
	public void writeWorldInput(){
		clearWorldLink();
		File folder = new File(currentPath);
		this.worldId.CreateStringWME("current-directory", folder.getName());
		Identifier objectsId = worldId.CreateIdWME("objects");
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			Identifier fileId = objectsId.CreateIdWME("object");
			fileId.CreateStringWME("name", file.getName());
			if(file.isFile()) fileId.CreateStringWME("type", "file");
			else if (file.isDirectory()) fileId.CreateStringWME("type", "folder");
		}
	}
	
	private void clearWorldLink(){
		deleteAllChildren(this.worldId);
	}
	
	private void deleteAllChildren(Identifier id){
		int index = 0;
		while (id.GetNumberChildren() > 0){
			WMElement child = id.GetChild(index);
			if (child != null) {
					child.DestroyWME();
				}
		}
	}
	
	
	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}
	
	

}
