package com.parc.docushare;

import java.io.IOException;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.ParseException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jdom2.JDOMException;

/**
 * @author fmasri
 * This class represents a file/folder found on Docushare. 
 * It tries to mirror all methods that Java object File has.
 */
public class DocushareFile {

	private String title;
	private int handle;
	private String type;
	
	/**
	 * @param title
	 * @param handle
	 * @param type
	 */
	public DocushareFile(String title, String handle, String type) {
		this.title = title;
		this.type = type;
		
		// Hanldes can be either a number or a conjunction of object type and a hadle
		//  60 == Folder-60 == Object that has handle 60 and is Folder
		if(handle.contains("-")) {
			String [] f = handle.split("-");
			this.handle = Integer.parseInt(f[f.length -1]);
		}
		else {
			this.handle = Integer.parseInt(handle);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		DocushareFile f = (DocushareFile) o;
		
		if(this.type.equals(f.type) && this.handle == f.handle && this.title.equals(f.title) ) {
			return true;
		}
		else {
			return false;
		}
		
	}

	/**
	 * Get HttpClient and HttpContext that is already authenticated and perform requests.
	 * @param httpclient
	 * @param context
	 * @return Set of Docushare files
	 */
	public Set<DocushareFile> listFiles(CloseableHttpClient httpclient, HttpClientContext context){
		
        ListFolder l = new ListFolder();
        l.setHttpClient(httpclient);
        l.setContext(context);
        
        	Set<DocushareFile> ret;
		try {
			ret = l.listFolderContent(this.handle);
			return ret;
		}
		catch (ParseException | XPathExpressionException | IOException | JDOMException e) {
			e.printStackTrace();
		}

		return null;
		
	}
	
	@Override
	public String toString() {
		return "Title: "+this.title+" handle: "+this.handle+" type: "+this.type;
	}
	
	
	public boolean isFile() {
		return this.type.equals("Document");
	}
	
	public boolean isDirectory() {
		return this.type.equals("Folder");
	}
	
	@Override
	public int hashCode() {
		
		
		return this.handle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getHandle() {
		return handle;
	}

	public void setHandle(int handle) {
		this.handle = handle;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
