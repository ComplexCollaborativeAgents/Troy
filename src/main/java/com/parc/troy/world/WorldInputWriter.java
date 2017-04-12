package com.parc.troy.world;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sml.Identifier;
import sml.WMElement;

import com.parc.troy.SoarHelper;

public class WorldInputWriter {
	
	private World world;
	private Identifier worldId;
	private WMElement currentFolderName;
	private Identifier objectsId;
	private Map<File, Identifier> fileIdMap;

	public WorldInputWriter(String dmName, Identifier worldId, World world){
		this.world = world;
		this.worldId = worldId;
		fileIdMap = new HashMap<File, Identifier>();
	}
	
	public void writeWorldInput(){
		if (!this.world.isCurrentPathSameAsPreviousPath()){
			//System.out.println("First time in this directory. Writing the world structure.");
			this.clearWorldLink();
			this.writeFolderStructure();
			this.world.setPreviousPath(this.world.getCurrentPath());
		}
		else
			this.updateFileFolderObjects();			
	}
	
	private void writeFolderStructure(){
		//System.out.println("Writing new folder structure");
		this.currentFolderName = this.worldId.CreateStringWME("current-folder", this.world.getFolder().getName());
		this.objectsId = this.worldId.CreateIdWME("objects");
		this.fileIdMap = new HashMap<File, Identifier>();
		Set<File> setOfFiles = this.world.getObjectSet();
		for(File file: setOfFiles){
			this.createAndAddFileIdentifier(file);
		}
	}
	
	private void updateFileFolderObjects(){
		//System.out.println("Already have the structure");
		Set<File> setOfFiles = this.world.getObjectSet();
		Set<File> filesToKeep = Collections.emptySet();
		//System.out.println("Number of items in fileIdMap " + this.fileIdMap.size());
		
		for (File file: setOfFiles){
			if(!this.fileIdMap.keySet().contains(file)){
				System.out.println("Adding file " + file.toString());
				this.createAndAddFileIdentifier(file);
			}
		}
		
		for (File file: this.fileIdMap.keySet()){
			if(!setOfFiles.contains(file)){
				//System.out.println("Deleting file " + file.toString());
				SoarHelper.deleteAllChildren(this.fileIdMap.get(file));
				this.fileIdMap.get(file).DestroyWME();
				this.fileIdMap.remove(file);
			}
		}
	}
	
	private void createAndAddFileIdentifier(File file){
		Identifier fileId = this.objectsId.CreateIdWME("object");
		fileId.CreateStringWME("name", file.getName());
		if(file.isFile()) fileId.CreateStringWME("type", "file_object");
		else if (file.isDirectory()) fileId.CreateStringWME("type", "folder_object");
		this.fileIdMap.put(file, fileId);
	}
	
	private void clearWorldLink(){
		if(this.currentFolderName != null){
			this.currentFolderName.DestroyWME();
		}
		
		if(this.objectsId != null){
			this.objectsId.DestroyWME();
		}
		
		if(this.fileIdMap != null){
			for (Map.Entry<File, Identifier> entry : this.fileIdMap.entrySet()) {
				SoarHelper.deleteAllChildren(entry.getValue());
			}
		}
	}
	

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public WMElement getCurrentFolderName() {
		return currentFolderName;
	}

	public void setCurrentFolderName(WMElement currentFolderName) {
		this.currentFolderName = currentFolderName;
	}

	public Map<File, Identifier> getFileIdMap() {
		return fileIdMap;
	}

	public void setFileIdMap(HashMap<File, Identifier> fileIdMap) {
		this.fileIdMap = fileIdMap;
	}
	
	

}
