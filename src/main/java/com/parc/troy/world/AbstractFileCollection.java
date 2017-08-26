package com.parc.troy.world;

import sml.Identifier;

public abstract class AbstractFileCollection {
	
	protected Identifier currentFolderId;
	protected abstract void processCutObject(String objectName, String objectType);
	protected abstract void processCopyObject(String objectName, String objectType);
	protected abstract void processPasteObject(String objectName, String objectType);
	protected abstract void deleteObject();
	protected abstract void processCreateFolder(String directoryName);
	protected abstract void processChangeFolder(Identifier folderId);
}
