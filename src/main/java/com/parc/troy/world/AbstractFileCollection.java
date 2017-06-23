package com.parc.troy.world;

import sml.WMElement;

public abstract class AbstractFileCollection {
	
	protected WMElement currentFolderNameId;

	protected abstract void processCutObject(String objectName, String objectType);
	protected abstract void processCopyObject(String objectName, String objectType);
	protected abstract void processPasteObject(String objectName, String objectType);
	protected abstract void deleteObject();
	protected abstract void processCreateFolder(String directoryName);
	protected abstract void processChangeFolder(String directory);
}
