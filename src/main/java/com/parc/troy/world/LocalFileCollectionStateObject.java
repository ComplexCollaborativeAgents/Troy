package com.parc.troy.world;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.parc.troy.SoarHelper;
import com.parc.troy.SoarInterface;
import com.parc.xi.dm.Config;

import sml.Identifier;
import sml.WMElement;

/**
 * This class maintains the current 'perceptible' state of a local filesystem as observed by a human
 * when in a directory. It maintains a list of all the visible files and folders. The current
 * perceptible state is written on a designated inputlink WME in the soar agent. It also implements
 * actions such as navigating, creating, deleting files and folders etc. These actions are applied
 * when the soar agent issues these commands in response to interactions with a human.
 * 
 * 
 * @author smohan
 * 
 */
/**
 * @author smohan
 *
 */
public class LocalFileCollectionStateObject extends AbstractFileCollection implements StateObject {

	private String currentPath;
	private String previousPath;
	private String homePath;
	
	
	private String selectedObjectPath;
	private String selectedObjectName;
	private String selectedObjectType;
	private File selectedObject;
	private Map<File, Identifier> selectedObjectIdentifierMap;
	private boolean shouldDeleteOldPath;

	private File folder;
	private Set<File> objectSet;
	private Map<File, Identifier> fileIdentifierMap;
	//private Identifier navigatedToFolderId;

	private static Logger LOGGER = Logger.getLogger(LocalFileCollectionStateObject.class
			.getName());

	public LocalFileCollectionStateObject(String dmName) {
		this.homePath = Config.getProperty(dmName + ".Home", null);
		previousPath = null;
		if (this.homePath != null) {
			currentPath = this.homePath;
			folder = new File(this.currentPath);
			objectSet = this.getSetofFilesFolders();
		}

		fileIdentifierMap = new HashMap<File, Identifier>();
		selectedObjectIdentifierMap = new HashMap<File, Identifier>();
		selectedObjectPath = null;
		selectedObjectName = null;
		selectedObjectType = null;
		selectedObject = null;
		shouldDeleteOldPath = false;
	//	navigatedToFolderId = null;
		
		currentFolderId = null;
	}

	private Set<File> getSetofFilesFolders() {
		Set<File> set = new HashSet<File>(Arrays.asList(folder.listFiles()));
		return set;
	}

	public void update() {
		this.folder = new File(this.currentPath);
		Set<File> newFileSet = this.getSetofFilesFolders();

		for (File newFile : newFileSet) {
			if (!this.objectSet.contains(newFile)) {
				this.objectSet.add(newFile);
			}
		}

		Iterator<File> iterFile = this.objectSet.iterator();
		while (iterFile.hasNext()) {
			File oldFile = iterFile.next();
			if (!newFileSet.contains(oldFile)) {
				iterFile.remove();
			}
		}

	}
	
	public void writeToSoar(Identifier stateId) {
		
		Identifier worldId = stateId;
		
		if (this.previousPath == null) { // the agent is in home directory
			writeHomeFolder(worldId);
			this.previousPath = this.currentPath;
		}
		else if (this.previousPath != this.currentPath) { // the agent is in a new directory
			writeNewFolder(worldId);
			this.previousPath = this.currentPath;
		}
		else
			updateFileFolderObjects(worldId);
		
		
		updateSelectedObject(worldId);
	}
	
	
	private void writeHomeFolder(Identifier worldId) {
		this.currentFolderId = worldId.CreateIdWME("current-folder");
		currentFolderId.CreateStringWME("name", folder.getName());
		currentFolderId.CreateStringWME("type", "folder_object");
		
		this.fileIdentifierMap = new HashMap<File, Identifier>();
		for (File file : this.objectSet) {
			this.createAndAddFileIdentifier(file, worldId);
		}
	}
	
	private void writeNewFolder(Identifier worldId) {
		Identifier navigatedToFolderId = null;
		Entry<File,Identifier> entryToRemove = null;
		
		for (Entry<File,Identifier> entry: this.fileIdentifierMap.entrySet()){
			if(Objects.equals(currentPath, entry.getKey().getAbsolutePath())) {
				navigatedToFolderId = entry.getValue();
				entryToRemove = entry;
			}
		}
		
		this.fileIdentifierMap.remove(entryToRemove.getKey());
		
		SoarHelper.deleteAllChildren(this.currentFolderId);
		this.currentFolderId.DestroyWME();
		
		this.currentFolderId = worldId.CreateSharedIdWME("current-folder", navigatedToFolderId);
		
		if (this.fileIdentifierMap != null && this.fileIdentifierMap.size() > 0) 
			this.deleteAllFileIdentifiers();
		entryToRemove.getValue().DestroyWME();
	
		this.fileIdentifierMap = new HashMap<File, Identifier>();
		
		for (File file : this.objectSet) {
			this.createAndAddFileIdentifier(file, worldId);
		}
		
	}

	private void deleteAllFileIdentifiers() {
		for (File file : this.fileIdentifierMap.keySet()) {
			if(!selectedObjectIdentifierMap.containsKey(file)) {
				SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
			}
			this.fileIdentifierMap.get(file).DestroyWME();
		}
	}

	private void createAndAddFileIdentifier(File file, Identifier worldId) {
		Identifier fileId = worldId.CreateIdWME("object");
		fileId.CreateStringWME("name", file.getName());
		if (file.isFile())
			fileId.CreateStringWME("type", "file_object");
		else if (file.isDirectory())
			fileId.CreateStringWME("type", "folder_object");
		this.fileIdentifierMap.put(file, fileId);
	}

	private void updateFileFolderObjects(Identifier worldId) {
		Set<File> setOfFiles = this.objectSet;
		for (File file : setOfFiles) {
			if (!this.fileIdentifierMap.keySet().contains(file)) {
				this.createAndAddFileIdentifier(file, worldId);
			}
		}
		
		HashSet<File> removeFileSet = new HashSet<File>();

		for (File file : this.fileIdentifierMap.keySet()) {
			if (!setOfFiles.contains(file)) {
				removeFileSet.add(file);
			}
		}
		
		for(File file: removeFileSet) {
			SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
			this.fileIdentifierMap.get(file).DestroyWME();
			this.fileIdentifierMap.remove(file);
		}
	}
	
	private void updateSelectedObject(Identifier worldId) {
		Identifier selectedObjectId = null;
		
		// Get what Soar agent thinks the selected object is
		for (int i = 0; i < worldId.GetNumberChildren(); i++) {
			Identifier childId = worldId.GetChild(i).ConvertToIdentifier();
			if(childId != null && childId.GetAttribute().equals("selected")){
				selectedObjectId = childId;
			}
		}
		
		// If the interface does not think there is a selected object, but Soar 
		// has a selected object, delete Soar's selected object
		if(selectedObject == null && selectedObjectId != null ) 
				selectedObjectId.DestroyWME();
	
		if(selectedObject != null && selectedObjectId == null) {
			if(selectedObjectIdentifierMap.size() != 0)
				LOGGER.error("There should be no selected object yet.");
			Identifier newSelectedObjectId = fileIdentifierMap.get(selectedObject);
			worldId.CreateSharedIdWME("selected", newSelectedObjectId);
			selectedObjectIdentifierMap.put(selectedObject, newSelectedObjectId);
		}
		
		if(selectedObject != null && selectedObjectId != null) {
			if(selectedObjectIdentifierMap.size() != 1) 
				LOGGER.error("There should be one key,value pair in the selectedObjectIdentifierMap");
		}
			
	}

	public void readSoarCommandAndApply(Identifier commandId,
			SoarInterface soarI) {
		String command = commandId.GetParameterValue("name");
		LOGGER.info("Received " + command + " command from Soar.");
		switch (command) {
		case "change-folder":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("folder_object")) {
					Identifier folderId = commandId.GetChild(i).ConvertToIdentifier();
					this.processChangeFolder(folderId);
				}
			}
			break;
		case "create-folder":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("folder_object")) {
					String folderName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCreateFolder(folderName);
				}
			}
			break;
		case "cut":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("folder_object") || objectType.equals("file_object")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCutObject(objectName, objectType);
				}
			}
			break;

		case "copy":
			LOGGER.info("Received copy command from Soar.");
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("folder_object") || objectType.equals("file_object")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCopyObject(objectName,objectType);
				}
			}
			break;

		case "paste":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("folder_object") || objectType.equals("file_object")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processPasteObject(objectName, objectType);
				}
			}
			break;

		default:
			LOGGER.error("Invalid command sent by Soar: " + command);
		}

		if (soarI != null) {
			soarI.getIdentifiersToRemove().add(commandId);
		}

	}
	
	protected void processCutObject(String objectName, String objectType) {
		selectedObjectPath = currentPath + "/" + objectName;
		selectedObjectName = objectName;
		selectedObjectType = objectType;
		
		for(File object : objectSet){
			if (object.getName().equals(objectName)) {
				selectedObject = object;
			}
		}
		LOGGER.info("Object copied to clipboard: " + objectName);
		
		shouldDeleteOldPath = true;
	}

	protected void processCopyObject(String objectName, String objectType) {
		selectedObjectPath = currentPath + "/" + objectName;
		selectedObjectName = objectName;
		selectedObjectType = objectType;
		
		for(File object : objectSet){
			if (object.getName().equals(objectName)) {
				selectedObject = object;
			}
		}
		
		if(selectedObject == null){
			LOGGER.error("Selected object is null, but should not be.");
		}
		LOGGER.info("Object copied to clipboard: " + objectName);
	}

	protected void processPasteObject(String objectName, String objectType) {
		if(selectedObjectPath == null || objectType == null || objectType != selectedObjectType){
			LOGGER.error("Trying to paste an object that does not exist.");
		}
		
		String finalPath = currentPath;
		if (selectedObjectType.equals("file_object")) {
			try {
				FileUtils.copyFileToDirectory(new File(selectedObjectPath), new File(
						finalPath));
			} catch (IOException e) {
				LOGGER.error("Either source: " + selectedObjectPath
						+ " or destination: " + finalPath + " is invalid.");
				e.printStackTrace();
			} catch (NullPointerException e) {
				LOGGER.error("Either source path: " + selectedObjectPath
						+ " or destination path: " + finalPath + " is null.");
			}
		}

		if (selectedObjectType.equals("folder_object")) {
			try {
				FileUtils.copyDirectoryToDirectory(new File(selectedObjectPath),
						new File(finalPath));
			} catch (IOException e) {
				LOGGER.error("Either source: " + selectedObjectPath
						+ " or destination: " + finalPath + " is invalid.");
				e.printStackTrace();
			} catch (NullPointerException e) {
				LOGGER.error("Either source path: " + selectedObjectPath
						+ " or destination path: " + finalPath + " is null.");
			}
		}
		
		if (shouldDeleteOldPath == true) deleteObject();

		setObjectNameToCopy(null);
		selectedObjectType = null;
		selectedObjectPath = null;
		setSelectedObject(null);
		shouldDeleteOldPath = false;
	}

	protected void deleteObject() {

		if (selectedObjectType.equals("folder_object")) {
			try {
				FileUtils.deleteDirectory(new File(selectedObjectPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (selectedObjectType.equals("file_object")) {
			File fileToDelete = new File(selectedObjectPath);
			fileToDelete.delete();
		}

	}

	protected void processCreateFolder(String directoryName) {
		String folderPath = currentPath + "/" + directoryName;
		File newFolder = new File(folderPath);
		newFolder.mkdir();
	}

	protected void processChangeFolder(Identifier folderId) {
		String folderName = folderId.GetParameterValue("name");
		String newCurrentPath = currentPath + "/" + folderName;
		currentPath = newCurrentPath;
		//navigatedToFolderId = folderId;
		
		LOGGER.debug("Changed to directory " + currentPath);
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
	
	public Map <File, Identifier> getFileIdentifierMap() {
		return fileIdentifierMap;
	}

	public void setFileIdentifierMap(Map<File, Identifier> map) {
		this.fileIdentifierMap = map;
	}

	public String getObjectNameToCopy() {
		return selectedObjectName;
	}

	public void setObjectNameToCopy(String objectNameToCopy) {
		this.selectedObjectName = objectNameToCopy;
	}
	
	public String getPathToCopy() {
		return selectedObjectPath;
	}
	
	public void setPathToCopy(String pathToCopy) {
		this.selectedObjectPath = pathToCopy;
	}
	
	public String getObjectTypeToCopy() {
		return selectedObjectType;
	}
	
	public void setObjectTypeToCopy(String objectTypeToCopy) {
		this.selectedObjectType = objectTypeToCopy;
	}
	
	
	public Boolean getShouldDeleteOldPath() {
		return shouldDeleteOldPath;
	}
	
	public void setShouldDeleteOldPath(Boolean bool) {
		this.shouldDeleteOldPath = bool;
	}

	public File getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(File selectedObject) {
		this.selectedObject = selectedObject;
	}

}
