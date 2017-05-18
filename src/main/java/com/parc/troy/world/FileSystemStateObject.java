package com.parc.troy.world;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
public class FileSystemStateObject implements StateObject {

	private String currentPath;
	private String previousPath;
	private String homePath;

	private File folder;
	private Set<File> objectSet;

	private WMElement currentFolderNameId;
	private Map<File, Identifier> fileIdentifierMap;

	private static Logger LOGGER = Logger.getLogger(FileSystemStateObject.class
			.getName());

	public FileSystemStateObject(String dmName) {
		this.homePath = Config.getProperty(dmName + ".Home", null);
		previousPath = null;
		if (this.homePath != null) {
			currentPath = this.homePath;
			folder = new File(this.currentPath);
			objectSet = this.getSetofFilesFolders();
		}

		fileIdentifierMap = new HashMap<File, Identifier>();
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
		if (command.equals("change-directory")) {
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("directory")) {
					String directory = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processChangeDirectory(directory);
				}
			}
		}

		if (command.equals("create-directory")) {
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				if (commandId.GetChild(i).GetAttribute().equals("directory")) {
					String directoryName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCreateDirectory(directoryName);
				}
			}
		}

		if (soarI != null) {
			soarI.getIdentifiersToRemove().add(commandId);
		}

	}

	private void processCreateDirectory(String directoryName) {
		String folderPath = currentPath + "/" + directoryName;
		File newFolder = new File(folderPath);
		newFolder.mkdir();
	}

	private void processChangeDirectory(String directory) {
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

}
