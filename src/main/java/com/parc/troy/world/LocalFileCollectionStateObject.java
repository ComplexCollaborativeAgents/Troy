package com.parc.troy.world;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
	
	
	private String pathToCopy;
	private String objectNameToCopy;
	private String objectTypeToCopy;
	private boolean shouldDeleteOldPath;

	private File folder;
	private Set<File> objectSet;

	private Map<File, Identifier> fileIdentifierMap;

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
		pathToCopy = null;
	}

	private Set<File> getSetofFilesFolders() {
		Set<File> set = new HashSet<File>(Arrays.asList(folder.listFiles()));
		return set;
	}

	private Boolean isCurrentPathSameAsPreviousPath() {
		if (this.previousPath == null)
			return false;
		if (this.currentPath == this.previousPath)
			return true;
		else
			return false;
	}

	public void update() {
		LOGGER.info("Updating the local filesystem state.");
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
		if (!this.isCurrentPathSameAsPreviousPath()) {
			// System.out.println("First time in this directory. Writing the world structure.");
			this.clearWorldLink();
			this.writeFolderStructure(worldId);
			previousPath = currentPath;
		} else
			this.updateFileFolderObjects(worldId);

	}

	private void clearWorldLink() {
		if (this.currentFolderNameId != null) {
			this.currentFolderNameId.DestroyWME();
		}

		if (this.fileIdentifierMap != null) {
			for (Map.Entry<File, Identifier> entry : this.fileIdentifierMap
					.entrySet()) {
				SoarHelper.deleteAllChildren(entry.getValue());
			}
		}
	}

	private void writeFolderStructure(Identifier worldId) {
		// System.out.println("Writing new folder structure");
		this.currentFolderNameId = worldId.CreateStringWME("current-folder",
				this.folder.getName());
		if (this.fileIdentifierMap != null && this.fileIdentifierMap.size() > 0) {
			this.deleteAllFileIdentifiers();
		}
		this.fileIdentifierMap = new HashMap<File, Identifier>();
		for (File file : this.objectSet) {
			this.createAndAddFileIdentifier(file, worldId);
		}
	}

	private void deleteAllFileIdentifiers() {
		for (File file : this.fileIdentifierMap.keySet()) {
			SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
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
		// System.out.println("Already have the structure");
		Set<File> setOfFiles = this.objectSet;
		// System.out.println("Number of items in fileIdMap "
		// + this.fileIdMap.size());

		for (File file : setOfFiles) {
			if (!this.fileIdentifierMap.keySet().contains(file)) {
				// System.out.println("Adding file " +
				// file.toString());
				this.createAndAddFileIdentifier(file, worldId);
			}
		}

		for (File file : this.fileIdentifierMap.keySet()) {
			if (!setOfFiles.contains(file)) {
				// System.out.println("Deleting file " +
				// file.toString());
				SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
				this.fileIdentifierMap.get(file).DestroyWME();
				this.fileIdentifierMap.remove(file);
			}
		}
	}

	public void readSoarCommandAndApply(Identifier commandId,
			SoarInterface soarI) {
		String command = commandId.GetParameterValue("name");
		switch (command) {
		case "change-folder":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("directory")) {
					String directory = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processChangeDirectory(directory);
				}
			}
			break;
		case "create-folder":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("folder")) {
					String folderName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCreateFolder(folderName);
				}
			}
			break;
		case "cut":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("folder") || objectType.equals("file")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCutObject(objectName, objectType);
				}
			}
			break;

		case "copy":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("folder") || objectType.equals("file")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCopyObject(objectName,objectType);
				}
			}
			break;

		case "paste":
			this.processPasteObject();
			break;

		default:
			LOGGER.error("Invalid command sent by Soar: " + command);
		}

		if (soarI != null) {
			soarI.getIdentifiersToRemove().add(commandId);
		}

	}
	
	protected void processCutObject(String objectName, String objectType) {
		pathToCopy = currentPath + "/" + objectName;
		objectNameToCopy = objectName;
		objectTypeToCopy = objectType;
		shouldDeleteOldPath = true;
	}

	protected void processCopyObject(String objectName, String objectType) {
		pathToCopy = currentPath + "/" + objectName;
		objectNameToCopy = objectName;
		objectTypeToCopy = objectType;
	}

	protected void processPasteObject() {
		String finalPath = currentPath;
		if (objectTypeToCopy.equals("file")) {
			try {
				FileUtils.copyFileToDirectory(new File(pathToCopy), new File(
						finalPath));
			} catch (IOException e) {
				LOGGER.error("Either source: " + pathToCopy
						+ " or destination: " + finalPath + " is invalid.");
				e.printStackTrace();
			} catch (NullPointerException e) {
				LOGGER.error("Either source path: " + pathToCopy
						+ " or destination path: " + finalPath + " is null.");
			}
		}

		if (objectTypeToCopy.equals("folder")) {
			try {
				FileUtils.copyDirectoryToDirectory(new File(pathToCopy),
						new File(finalPath));
			} catch (IOException e) {
				LOGGER.error("Either source: " + pathToCopy
						+ " or destination: " + finalPath + " is invalid.");
				e.printStackTrace();
			} catch (NullPointerException e) {
				LOGGER.error("Either source path: " + pathToCopy
						+ " or destination path: " + finalPath + " is null.");
			}
		}
		
		if (shouldDeleteOldPath == true) deleteObject();

		setObjectNameToCopy(null);
		objectTypeToCopy = null;
		pathToCopy = null;
		shouldDeleteOldPath = false;
	}

	protected void deleteObject() {

		if (objectTypeToCopy.equals("folder")) {
			try {
				FileUtils.deleteDirectory(new File(pathToCopy));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (objectTypeToCopy.equals("file")) {
			File fileToDelete = new File(pathToCopy);
			fileToDelete.delete();
		}

	}

	protected void processCreateFolder(String directoryName) {
		String folderPath = currentPath + "/" + directoryName;
		File newFolder = new File(folderPath);
		newFolder.mkdir();
	}

	protected void processChangeDirectory(String directory) {
		String newCurrentPath = currentPath + "/" + directory;
		currentPath = newCurrentPath;
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

	public String getObjectNameToCopy() {
		return objectNameToCopy;
	}

	public void setObjectNameToCopy(String objectNameToCopy) {
		this.objectNameToCopy = objectNameToCopy;
	}
	
	public String getPathToCopy() {
		return pathToCopy;
	}
	
	public void setPathToCopy(String pathToCopy) {
		this.pathToCopy = pathToCopy;
	}
	
	public String getObjectTypeToCopy() {
		return objectTypeToCopy;
	}
	
	public void setObjectTypeToCopy(String objectTypeToCopy) {
		this.objectTypeToCopy = objectTypeToCopy;
	}
	
	
	public Boolean getShouldDeleteOldPath() {
		return shouldDeleteOldPath;
	}
	
	public void setShouldDeleteOldPath(Boolean bool) {
		this.shouldDeleteOldPath = bool;
	}

}
