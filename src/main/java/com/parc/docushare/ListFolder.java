package com.parc.docushare;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author fmasri
 * List children of a specific folder
 */
public class ListFolder {

	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();

    public Set<DocushareFile> listFolderContent( int handle ) throws ClientProtocolException, IOException, ParseException, XPathExpressionException, JDOMException {
        
		String resourceUrl = String.format("https://" + server + shadowpath
				+ "object/%d/xcmAPI/children", handle);
        
		try {
			
			HttpGet req = new HttpGet(resourceUrl);
			HttpResponse response = httpclient.execute( req , context );
			HttpEntity result = response.getEntity();
			
	            
	        return listContent( result );
	        

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		

        return null;
    }

    /**
     * Interate over file/folder elements in the response and create DocushareFile objects from them
     * @param entity - response
     * @return
     * @throws XPathExpressionException
     * @throws ParseException
     * @throws IOException
     */
    private Set<DocushareFile> listContent( HttpEntity entity ) throws XPathExpressionException, ParseException, IOException {
        
    		String xml = EntityUtils.toString( entity );
    		
    		InputSource inputSource = new InputSource( new StringReader( xml ) );
    		XPath xpath = XPathFactory.newInstance().newXPath();
    		
    		NodeList nodeList = (NodeList) xpath.evaluate( "//found/objects/object", inputSource, XPathConstants.NODESET );
    		
    		Set<DocushareFile> ret = new HashSet<DocushareFile>();
    		
    		for (int i = 0; i < nodeList.getLength(); i++) {
    		    Node node = nodeList.item(i);
    		    String handle =(String) xpath.evaluate("@handle", node, XPathConstants.STRING);
    		    String title =(String) xpath.evaluate("title", node, XPathConstants.STRING);
    		    String type =(String) xpath.evaluate("@classType", node, XPathConstants.STRING);
    		    DocushareFile f = new DocushareFile(title, handle, type);
			//System.out.println("Child "+f.toString());
    		    ret.add(f);
    		    
    		}
    		
    		return ret;


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

    public static void main( String[] args ) throws Exception {
        ListFolder test = new ListFolder();
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();
        
        try {
        	
        		test.listFolderContent(61);

		} finally {
			test.httpclient.close();
		}
    }

}
