package com.parc.docushare;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Random;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;

/**
 * @author fmasri
 * Class used for creating folder on docushareflex
 */
public class CreateFolder {

	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();
    
    // XML that is added to the POST request that contains import information for creating the file
	String createFolderXml;
	{
		try {
			createFolderXml = FileUtils.readFileToString(new File(
					"/Users/fmasri/Desktop/Troy/src/main/resources/docushareXml/createFolder.xml"), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create new folder on the DocushareFlex
	 * @param parent - handle of the parent folder
	 * @param owner - handle of the owner
	 * @param title - String title of the folder
	 * @return integer - handle of the new folder
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public int createFolder(int parent, int owner, String title)
			throws ClientProtocolException, IOException {

		HttpPost req = new HttpPost("https://" + server + shadowpath
				+ "falcon/objects");
		
		// Passing parameters to the String (%s fields in the XML are to be filled) 
		String createObj = String.format(createFolderXml, title, parent, owner);
		
		
		// Passing the raw data to the request (letting know it is application/xml)
		StringEntity entity = new StringEntity(createObj,
				ContentType.APPLICATION_XML);
		req.setEntity(entity);

		// Send the request
		CloseableHttpResponse response = httpclient.execute(req, this.context);

		try {
			
			int status = response.getStatusLine().getStatusCode();
			// System.out.println("status: " + status);

			if (status >= 200 && status < 300) {
				HttpEntity result = response.getEntity();
				
				// Parsing new id (handle) from the responses raw data (xml) 
				return Util.getHandle(result);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} finally {
			response.close();
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
		
		CreateFolder test = new CreateFolder();
		
		final Random random = new Random();
		
		// creating the random BigInteger in the range ( 0 , 2^31-1 )
		String folderTitle = "Test Folder - "
				+ new BigInteger(130, random).toString(32);
		
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();
        
		
        try {
            // Create new folder in Folder-61 owned by User-537
			int newFolder = test.createFolder(61, 537, folderTitle);
			System.out.println("New Folder Handle: " + newFolder);
		} finally {
			test.httpclient.close();
		}
	}

}
