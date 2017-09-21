package com.parc.docushare;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jdom2.Document;
import org.jdom2.JDOMException;

/**
 * @author fmasri
 * Delete one or multiple objects by their handle
 */
public class DeleteObjects {
	
	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();
	
	
	// Get an object's properties
	/** Remove objects by their handles
	 * @param handles - As String parameters separated by commas
	 * @return Document - JDOM representation of a document
	 * @throws XPathExpressionException
	 */
	public Document removeObject( int handle ) throws XPathExpressionException {
		String resourceUrl = String.format("https://" + server + shadowpath
				+ "objects/%s/falcon", handle);
		try {
			
		    HttpDelete req = new HttpDelete( resourceUrl );
			HttpResponse response = httpclient.execute( req , context );
			HttpEntity result = response.getEntity();
			Document doc = Util.parse(result);
			
			// Printing the contents of the deleted documents
			//Util.printJDOM(doc);

			return doc;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		return null;
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
		DeleteObjects test = new DeleteObjects();
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();
		
		// Delete multiple objects "125,126,127"
		test.removeObject(543);
		
	}
}
