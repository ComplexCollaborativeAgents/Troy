package com.parc.troy.world;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import com.parc.docushare.CreateFolder;
import com.parc.docushare.DeleteObjects;
import com.parc.docushare.DocushareFile;
import com.parc.docushare.GetDocuShareFile;
import com.parc.docushare.GetRootFolderHandle;
import com.parc.docushare.HttpClientLogin;
import com.parc.docushare.MoveDocument;
import com.parc.troy.SoarHelper;
import com.parc.troy.SoarInterface;

import sml.Identifier;

/**
 * @author fmasri
 * DocuShareCollectionStateObject represents the Docushare view of a specific user
 */
public class DocuShareCollectionStateObject extends AbstractFileCollection implements StateObject{
	

	private int userId;
	
	// Not using paths, but handles (ids) of the objects
	private int currentDirId = -1 ;
	private int previousDirId = -1;
	private int homeDirId = -1;
	
	private DocushareFile folder;
	
	// Selected object params
	private int selectedObjectDirId;
	private String selectedObjectType;
	private DocushareFile selectedObject;
	
	private Map<DocushareFile, Identifier> selectedObjectIdentifierMap;
	//private boolean shouldDeleteOldPath;

	private Set<DocushareFile> objectSet;
	private Map<DocushareFile, Identifier> fileIdentifierMap;
	
	// Having a special mapper between title -> handle. Soar knows only the titles of the folders/files. I must map back.
	private Map<String, Integer> titleToHandle;
	
	//private Identifier navigatedToFolderId;
	private int pasteObjectDirId;
	private Identifier pasteObjectId;
	
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
	
	
	private static Logger LOGGER = Logger.getLogger(LocalFileCollectionStateObject.class
			.getName());
	
	/**
	 * @param userHandle - so far the user handle is needed when creating the collection - is it not possible to get the user handle from some Authentication step? The root folder is not owned by the user ...
	 */
	public DocuShareCollectionStateObject(int userHandle){
		
		this.userId = userHandle;
		
		// Get the handle of the root directory and set it as home
		GetRootFolderHandle g = new GetRootFolderHandle();
		
		try {
			this.httpclient = HttpClientLogin.getHttpClient("parc", "Password1!");
			this.context = HttpClientLogin.getContext();
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		g.setHttpClient(this.httpclient);
		g.setContext(this.context);
		        			
		this.homeDirId = g.getRootFolder();

		
		this.previousDirId = -1;
		
		if (this.homeDirId != -1) {
			this.currentDirId = this.homeDirId;
			
			GetDocuShareFile f = new GetDocuShareFile();
			f.setHttpClient(this.httpclient);
			f.setContext(this.context);

			// Getting the name of the root folder
			this.folder = f.getDocushareFile(this.currentDirId);
	
			this.objectSet = this.getSetofFilesFolders(); // how to list files of a folder
		}
		
		
		this.fileIdentifierMap = new HashMap<DocushareFile, Identifier>();
		this.titleToHandle = new HashMap<String, Integer>();
		
		this.selectedObjectIdentifierMap = new HashMap<DocushareFile, Identifier>();
		this.selectedObjectDirId = -1;
		this.selectedObjectType = null;
		this.selectedObject = null;
		//this.shouldDeleteOldPath = false;
		
		currentFolderId = null;
	}
	
	
	/**
	 * @return set of DocushareFile objects files and folders located in the current directory
	 */
	private Set<DocushareFile> getSetofFilesFolders() {
		
        return folder.listFiles(this.httpclient, this.context);
		
	}
	
	/* (non-Javadoc)
	 * @see com.parc.troy.world.StateObject#update()
	 * Soar periodically forces the virtual collections to update its status from the real environments
	 */
	@Override
	public void update() {
		
		GetDocuShareFile f = null;

		f = new GetDocuShareFile();
		f.setHttpClient(this.httpclient);
		f.setContext(this.context);

		this.folder = f.getDocushareFile(this.currentDirId);

		// Would not be enough just to get list and replace the previous one? Now the old ones are deleted and new once are added
		
		Set<DocushareFile> newFileSet;

		newFileSet = this.getSetofFilesFolders();

		for (DocushareFile newFile : newFileSet) {
			if (!this.objectSet.contains(newFile)) {
				this.objectSet.add(newFile);
			}
		}

		Iterator<DocushareFile> iterFile = this.objectSet.iterator();
		while (iterFile.hasNext()) {
			DocushareFile oldFile = iterFile.next();
			if (!newFileSet.contains(oldFile)) {
				iterFile.remove();
			}
		}

	}
	
	/* (non-Javadoc)
	 * @see com.parc.troy.world.StateObject#writeToSoar(sml.Identifier)
	 * Send changes that happened in the real environment to soar
	 */
	@Override
	public void writeToSoar(Identifier stateId) {
		
		Identifier worldId = stateId;
		
		if (this.previousDirId == -1) { // the agent is in home directory
			writeHomeFolder(worldId);
			this.previousDirId = this.currentDirId;
		}
		else if (this.previousDirId != this.currentDirId) { // the agent is in a new directory
			writeNewFolder(worldId);
			this.previousDirId = this.currentDirId;
		}
		else
			updateFileFolderObjects(worldId);
			
		updateSelectedObject(worldId);
		
	}
	
	/**
	 *  If new files appeared - I have to create Soar IDs.
	 *  If some files were deleted then I have to remove their Soar IDs
	 * @param worldId
	 */
	private void updateFileFolderObjects(Identifier worldId) {
		Set<DocushareFile> setOfFiles = this.objectSet;
		for (DocushareFile file : setOfFiles) {
			if (!this.fileIdentifierMap.keySet().contains(file)) {
				if( file.getHandle() == pasteObjectDirId) {
					//System.out.println("file identifier " + pasteObjectId.GetValueAsString());
					Identifier newId = worldId.CreateSharedIdWME("object", pasteObjectId);
					this.fileIdentifierMap.put(file, newId);
					this.titleToHandle.put(file.getTitle(), file.getHandle());
				} 
				else
					this.createAndAddFileIdentifier(file, worldId);
			}
		}
		
		HashSet<DocushareFile> removeFileSet = new HashSet<DocushareFile>();

		for (DocushareFile file : this.fileIdentifierMap.keySet()) {
			if (!setOfFiles.contains(file)) {
				removeFileSet.add(file);
			}
		}
		
		for(DocushareFile file: removeFileSet) {
			SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
			this.fileIdentifierMap.get(file).DestroyWME();
			this.titleToHandle.remove(file.getTitle());
			this.fileIdentifierMap.remove(file);
			
		}
	}
	
	/**
	 * Create Soar IDs for the home folder and its children. Soar must know about them in order to reason about them...
	 * @param worldId
	 */
	private void writeHomeFolder(Identifier worldId) {
		this.currentFolderId = worldId.CreateIdWME("current-folder");
		
		currentFolderId.CreateStringWME("name", this.folder.getTitle());
		currentFolderId.CreateStringWME("type", "folder_object");
		
		this.fileIdentifierMap = new HashMap<DocushareFile, Identifier>();
		this.titleToHandle = new HashMap<String, Integer>();
		for (DocushareFile file : this.objectSet) {
			this.createAndAddFileIdentifier(file, worldId);
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
			Identifier objectId = worldId.CreateSharedIdWME("selected", newSelectedObjectId);
			selectedObjectIdentifierMap.put(selectedObject, objectId);
		}
		
		if(selectedObject != null && selectedObjectId != null) {
			if(selectedObjectIdentifierMap.size() != 1) 
				LOGGER.error("There should be one key,value pair in the selectedObjectIdentifierMap");
		}
			
	}
	
	/**
	 * Create Soar IDs and specific parameters that would enable reasoning about them
	 * @param file
	 * @param worldId
	 */
	private void createAndAddFileIdentifier(DocushareFile file, Identifier worldId) {
		Identifier fileId = worldId.CreateIdWME("object");
		fileId.CreateStringWME("name", file.getTitle());
		if (file.isFile())
			fileId.CreateStringWME("type", "file_object");
		else if (file.isDirectory())
			fileId.CreateStringWME("type", "folder_object");
		this.fileIdentifierMap.put(file, fileId);
		this.titleToHandle.put(file.getTitle(), file.getHandle());
	}
	
	/**
	 * Changing to a different directory. 
	 * Soar must destroy the IDs of the previous directory and create IDs of the current directory.
	 * @param worldId
	 */
	private void writeNewFolder(Identifier worldId) {
		Identifier navigatedToFolderId = null;
		Entry<DocushareFile,Identifier> entryToRemove = null;
		
		for (Entry<DocushareFile,Identifier> entry: this.fileIdentifierMap.entrySet()){
			if(Objects.equals(currentDirId, entry.getKey().getHandle())) {
				navigatedToFolderId = entry.getValue();
				entryToRemove = entry;
			}
		}
		
		this.titleToHandle.remove(entryToRemove.getKey().getTitle());
		this.fileIdentifierMap.remove(entryToRemove.getKey());
		
		SoarHelper.deleteAllChildren(this.currentFolderId);
		this.currentFolderId.DestroyWME();
		
		this.currentFolderId = worldId.CreateSharedIdWME("current-folder", navigatedToFolderId);
		
		if (this.fileIdentifierMap != null && this.fileIdentifierMap.size() > 0) 
			this.deleteAllFileIdentifiers();
		entryToRemove.getValue().DestroyWME();
	
		this.fileIdentifierMap = new HashMap<DocushareFile, Identifier>();
		this.titleToHandle = new HashMap<String, Integer>();
		
		for (DocushareFile file : this.objectSet) {
			this.createAndAddFileIdentifier(file, worldId);
		}
		
	}
	
	private void deleteAllFileIdentifiers() {
		for (DocushareFile file : this.fileIdentifierMap.keySet()) {
			if(!selectedObjectIdentifierMap.containsKey(file)) {
				SoarHelper.deleteAllChildren(this.fileIdentifierMap.get(file));
			}
			this.fileIdentifierMap.get(file).DestroyWME();
		}
	}

	@Override
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
			System.out.println("Creating folder");
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
				if (objectType.equals("file_object")) {
					String objectName = commandId.GetChild(i)
							.ConvertToIdentifier().GetParameterValue("name");
					this.processCutObject(objectName, objectType);
				}
			}
			break;
		case "copy":
			break;

		case "paste":
			for (int i = 0; i < commandId.GetNumberChildren(); i++) {
				String objectType = commandId.GetChild(i).GetAttribute();
				if (objectType.equals("file_object")) {
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

	/* (non-Javadoc)
	 * @see com.parc.troy.world.AbstractFileCollection#processCutObject(java.lang.String, java.lang.String)
	 * I just mark it as selected!!! Do not keep the content. I could download it...The paste then moves the file.
	 * In local system cut would copy ot to the clipboard.
	 */
	@Override
	protected void processCutObject(String objectName, String objectType) {
		
		this.selectedObjectType = objectType;
		this.selectedObjectDirId = this.currentDirId;
		
		for(DocushareFile object : objectSet){
			if (object.getTitle().equals(objectName)) {
				this.selectedObject = object;
			}
		}
		LOGGER.info("Object copied to clipboard: " + objectName);
		
		//shouldDeleteOldPath = true;
		
	}

	@Override
	protected void processCopyObject(String objectName, String objectType) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.parc.troy.world.AbstractFileCollection#processPasteObject(java.lang.String, java.lang.String)
	 * Only move the selected object
	 */
	@Override
	protected void processPasteObject(String objectName, String objectType){

		if(this.selectedObjectDirId == -1 || objectType == null || !objectType.equals(this.selectedObjectType)){
			LOGGER.error("Trying to paste an object that does not exist.");
		}
		
		
		if (this.selectedObjectType.equals("file_object")) {
			
			/*try {
		        DownloadDocument d = new DownloadDocument();
		        
		        d.setHttpClient(HttpClientLogin.getHttpClient("parc", "Password1!"));
		        d.setContext(HttpClientLogin.getContext());

		        try {
		            d.download( this.selectedObject.getHandle() );
		        }
		        catch(MimeTypeException ex) {
		        	ex.printStackTrace();
		        }
		        finally {
		            d.getHttpClient().close();
		        }
		        
		        
		        CreateDocument c = new CreateDocument();
		        
		        c.setHttpClient(HttpClientLogin.getHttpClient("parc", "Password1!"));
		        c.setContext(HttpClientLogin.getContext());

		        File toBoUploaded = new File( "/Users/fmasri/Desktop/Troy/src/main/resources/tmp" ).listFiles()[0];
		        
		        int newHandle = c.uploadDocument( this.currentDirId, objectName,
		                toBoUploaded.toPath(), this.userId );
		        
		        c.getHttpClient().close();
		        toBoUploaded.delete();
		        
		        
			}
		    catch(ClientProtocolException ex) {
		    		ex.printStackTrace();
		    }

		   catch(IOException ex) {
		        	ex.printStackTrace();
		   } catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} catch (KeyStoreException ex) {
			ex.printStackTrace();
		}*/
			
		MoveDocument m = new MoveDocument();
	    try {
			m.setHttpClient(HttpClientLogin.getHttpClient("parc", "Password1!"));
		} catch (NoSuchAlgorithmException | KeyStoreException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    m.setContext(HttpClientLogin.getContext());
			
	    m.moveDocument( this.selectedObject.getHandle() , this.currentDirId);
	    
		LOGGER.info("Pasted: " + objectName);
		        
		}
		
		pasteObjectDirId = this.currentDirId;
		pasteObjectId = this.selectedObjectIdentifierMap.get(selectedObject);
		//System.out.println("paste object arg " + pasteObjectId.GetAttribute() + " val " + pasteObjectId.GetValueAsString());
		
		// No need, it was moved
		//if (shouldDeleteOldPath == true) deleteObject();

		this.selectedObjectType = null;
		this.selectedObject = null;
		this.selectedObjectDirId = -1;
		//shouldDeleteOldPath = false;
		
	}

	/* (non-Javadoc)
	 * @see com.parc.troy.world.AbstractFileCollection#deleteObject()
	 * Deleted the selected object
	 */
	@Override
	protected void deleteObject() {
		
		try {
		DeleteObjects d = new DeleteObjects();
		
        d.setHttpClient(HttpClientLogin.getHttpClient("parc", "Password1!"));
        d.setContext(HttpClientLogin.getContext());

        //System.out.println("Removing selected: "+selectedObject.getHandle());
        
		d.removeObject(selectedObject.getHandle());
		
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}


	@Override
	protected void processCreateFolder(String directoryName) {

		CreateFolder f = new CreateFolder();
		try {
		f.createFolder(this.currentDirId, this.userId , directoryName);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
	}

	@Override
	protected void processChangeFolder(Identifier folderId) {
		
		String folderName = folderId.GetParameterValue("name");
		
		int newCurrentDirId = this.titleToHandle.get(folderName);
		this.currentDirId = newCurrentDirId;
		//navigatedToFolderId = folderId;
		
		
		
		LOGGER.info("Changed to directory " + currentDirId);
		
	}

}
