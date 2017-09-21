package com.parc.docushare;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jdom2.Document;
import org.jdom2.JDOMException;

/**
 * @author fmasri
 * Given a user handle find the handle of its root directory
 */
public class GetRootFolderHandle {

	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();
	
	public int getRootFolder(){
		String resourceUrl = String.format("https://" + server + shadowpath
				+ "falcon/root");
		try {
			
			HttpGet req = new HttpGet(resourceUrl);
			HttpResponse response = httpclient.execute( req , context );
			HttpEntity result = response.getEntity();
			
			int rootHandle = Util.getRootHandle(result);
			// Printing the contents of the deleted documents
			return rootHandle;
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
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
	
	public static void main(String args[]) throws Exception{
		
		GetRootFolderHandle test = new GetRootFolderHandle();
		
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();
        
		
        try {
        	
        		System.out.println(test.getRootFolder());

		} finally {
			test.httpclient.close();
		}
	}
}

