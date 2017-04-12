package com.parc.troy.world;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.parc.xi.dm.Config;

public class World {
	private String currentPath;
	private String previousPath;
	private String homePath;
	
	private File folder;
	private Set<File> objectSet;
	
	public World(String dmName){
		// SM: put in a test if this file is not null.
		this.homePath = Config.getProperty(dmName +".Home",null);;
		currentPath = this.homePath;
		previousPath = null;
		
		folder = new File(this.currentPath);
		objectSet = this.getSetofFilesFolders();
	}
	
	public Boolean isCurrentPathSameAsPreviousPath(){
		if (this.previousPath == null)
			return false;
		if (this.currentPath == this.previousPath)
			return true;
		else
			return false;
	}
	
	public void updateWorld(){
		System.out.println("Updating world");
		Set<File> newFileSet = this.getSetofFilesFolders();
		System.out.println("Number of new files: " + newFileSet.size());
		
		// add new files
		for(File newFile: newFileSet){
			if(!this.objectSet.contains(newFile)){
				this.objectSet.add(newFile);
			}
		}
		
		// remove deleted files
		for(File oldFile: this.objectSet){
			if(!newFileSet.contains(oldFile)){
				this.objectSet.remove(oldFile);
			}
		}
		
		System.out.println("Number of objects: " + this.objectSet.size());
		
	}
	
	private Set<File> getSetofFilesFolders(){
		Set<File> set = new HashSet<File>(Arrays.asList(folder.listFiles()));
		return set;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	public String getPreviousPath() {
		return previousPath;
	}

	public void setPreviousPath(String previousPath) {
		this.previousPath = previousPath;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public Set<File> getObjectSet() {
		return objectSet;
	}

	public void setObjectSet(Set<File> objectList) {
		this.objectSet = objectList;
	}
	

}
