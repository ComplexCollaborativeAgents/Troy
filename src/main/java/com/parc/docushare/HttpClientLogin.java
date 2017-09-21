package com.parc.docushare;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;


/**
 * @author fmasri 
 * An example that uses HttpClient to execute an HTTP request,
 * that requires authentication, against Falcon.
 *
 */
public class HttpClientLogin {
	
	
    private static final String server = "parcdsflexdev.docushareflex.com";
    private static final String shadowpath = "/xcm/v1/shadow/";
    private static final String realm = "Falcon";
	
	private static CloseableHttpClient httpclient = null;
	
	/*
	 * First the client sends GET to server without credentials
	 * Second the server replies with - 401 UnAuthorized - Authenticate (with possible authentication methods)
	 * Third the client takes the base64 encoded credentials and sends them
	 * Fourth the server verifies the credentials and sends success code
	 * Context is needed in order to keep the session of the communication? No cache -> does not work.
	 */
	private static HttpClientContext context = HttpClientContext.create();
	
	/**
	 * Setting the authentication information in HttpClient
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	private static void setCredentials(String username, String password) throws IOException, NoSuchAlgorithmException, KeyStoreException {
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		
		credsProvider.setCredentials(new AuthScope( server, 443, realm , "basic" ),
				new UsernamePasswordCredentials( username, password ));

		HttpClientLogin.httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		
		
		//Until the end of catch try block - bypass of expired certificate 
		SSLContextBuilder builder = new SSLContextBuilder();
		SSLConnectionSocketFactory sslSF = null;
	    builder.loadTrustMaterial(null, new TrustStrategy	() {
	    	
	    	
		        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		            return true;
		        }
		    });


	    try {
	    	
			sslSF = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			
		} catch (KeyManagementException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	    
	    // Setting the cache for continuing with the basicauthentication 
	    HttpHost targetHost = new HttpHost( "parcdsflexdev.docushareflex.com" , 443, "https" );
	    AuthCache authCache = new BasicAuthCache();
	    authCache.put(targetHost, new BasicScheme());

	  
	    HttpClientLogin.context.setCredentialsProvider( credsProvider );
	    HttpClientLogin.context.setAuthCache( authCache );
	    
	    httpclient = HttpClients.custom().setSSLSocketFactory( sslSF ).setDefaultCredentialsProvider( credsProvider )
	    		.build();
	    

	    
	}
	
	/*
	 * Sending the GET method for authentication
	 * @param handle
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException*/

	public void get( String handle) throws IOException, NoSuchAlgorithmException, KeyStoreException {
		
		String resourceUrl = String.format( "https://" + server + shadowpath
				+ "object/%s/falcon/properties" , handle );

		try {
			
			HttpGet httpget = new HttpGet( resourceUrl );

			CloseableHttpResponse response = httpclient.execute( httpget , HttpClientLogin.context );
			
			HttpEntity entity = response.getEntity();
			
	    		String xml = EntityUtils.toString( entity );
	    		
	    		System.out.println(xml);
	    		
	    		InputSource inputSource = new InputSource( new StringReader( xml ) );
	    		XPath xpath = XPathFactory.newInstance().newXPath();
	    		
	    		
			
			// System.out.println("Status: " + response.getStatusLine());
			// System.out.println("isRepeatable: " + entity.isRepeatable());
			// System.out.println("isStreaming: " + entity.isStreaming());
			// System.out.println("getContentEncoding:" + ContentType.getOrDefault(entity));

			// Parse entity content
			// Get something from the parsed XML result
			// Ensure response body is fully consumed and closed
			// EntityUtils.consume(entity); response.close();

			
			} 
		
		catch (ClientProtocolException e) { 
			e.printStackTrace();
		} 
		catch (IOException e) { 
			e.printStackTrace();
		}
		finally { 
			httpclient.close();
		}
	}
	
	public static String getServer() {
		
		return HttpClientLogin.server;
		
	}
	
	public static String getShadowPath() {
		
		return HttpClientLogin.shadowpath;
		
	}
	
	public static CloseableHttpClient getHttpClient(String username, String password) throws NoSuchAlgorithmException, KeyStoreException, IOException {
		HttpClientLogin.setCredentials(username, password);
		return httpclient;
		
	}
	
	public static HttpClientContext getContext() {
		return HttpClientLogin.context;
	}


public static void main( String[] args ) throws Exception {
	

	HttpClientLogin test = new HttpClientLogin(); 
	HttpClientLogin.setCredentials("username", "password");
	test.get( "537" );

}

}
