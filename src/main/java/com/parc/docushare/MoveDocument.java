package com.parc.docushare;


import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jdom2.JDOMException;


/**
 * @author fmasri
 * Move document from one folder to another
 */
public class MoveDocument {
	
	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();
	
    // XML that is added to the POST request that contains import information for creating the file
	String moveDocumentXml;
	{
		try {
			moveDocumentXml = FileUtils.readFileToString(new File(
					"/Users/fmasri/Desktop/Troy/src/main/resources/docushareXml/moveDocument.xml"), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Move the document to a different folder.
	 * @param handle - integer handle of the current document
	 * @param parent - integer handle of the new folder
	 * @return integer - return the status code of moving
	 */
	public int moveDocument(int handle, int parent) {
		try {
			String resourceUrl = String.format("https://" + server + shadowpath
					+ "object/%d/falcon/properties", handle);
			
			HttpPost req = new HttpPost(resourceUrl);
			
	        String createObj = String.format( moveDocumentXml, parent);
			
			StringEntity entity = new StringEntity(createObj,
					ContentType.APPLICATION_XML);
			
			req.setEntity(entity);
			
			HttpResponse response = httpclient.execute(req, context);
			
			HttpEntity result = response.getEntity();
			if (result != null) {
				Util.parse(result);
			}
			return response.getStatusLine().getStatusCode();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void setHttpClient(CloseableHttpClient client) {
		this.httpclient =  client;
		
	}
	
	public void setContext(HttpClientContext context) {
		this.context = context;
		
	}
	
	public CloseableHttpClient getHttpClient() {
		return this.httpclient;
	}
	
	public HttpClientContext getContext() {
		return this.context;
	}
	
	public static void main(String[] args) throws Exception {
		MoveDocument test = new MoveDocument();
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();
		
        System.out.println(test.moveDocument( 542 , 539));

		
	}
}

