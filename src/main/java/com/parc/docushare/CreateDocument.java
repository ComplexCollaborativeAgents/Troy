package com.parc.docushare;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;


/**
 * @author fmasri
 * Class for creating Document in the DocushareFlex
 */
public class CreateDocument {

    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();

    // The updating information is provided in XML document that is going to be posted to the server through the REST API
    public static String createDocumentXml;
    {
        try {
            createDocumentXml = FileUtils.readFileToString( new File(
                    "/Users/fmasri/Desktop/Troy/src/main/resources/docushareXml/createDocument.xml" ) , "UTF-8" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Uploading the document
     * @param parent - handle of the parent folder
     * @param title - String title of the document
     * @param source - source path on the local system
     * @param owner - handle of the owner
     * @return integer handle of the new document
     * @throws ClientProtocolException
     * @throws IOException
     */
    public int uploadDocument( int parent, String title, Path source, int owner )
            throws ClientProtocolException, IOException {

    		// URI specific for creating new objects
        String resource = "https://" + server + shadowpath
                + "falcon/object/create";
        
        HttpPost req = new HttpPost( resource );
        
        // Read file path to File and then create FileBody (FileBody serves as part of the MultiPart object)
        FileBody content = new FileBody( source.toFile() );

        // Fill the XML string with missing parameter values (%s) - source - is a file path.
        String createObj = String.format( createDocumentXml, title, parent,
                source, owner );
        
        // Creating String body cause it is part of the MultiPart object
        StringBody request = new StringBody( createObj, ContentType.APPLICATION_XML);

        // Boundary - the server would not know how to split the data because its MultiPart
        String boundary = UUID.nameUUIDFromBytes( source.toString().getBytes() )
                .toString();

        // So - creating boundary from the file path
        HttpEntity entity = MultipartEntityBuilder.create()
                .setBoundary( boundary )
                .setCharset( Charset.forName( "UTF-8" ) )
                .setMode( HttpMultipartMode.BROWSER_COMPATIBLE )
                .addPart( "content", content )
                .addPart( "request", request )
                .build();

        req.setEntity( entity );
        CloseableHttpResponse response = httpclient.execute( req , context );

        try {
            int status = response.getStatusLine().getStatusCode();

            if ( status >= 200 && status < 300 ) {
            	
            		// if successful then return the handle ( id ) of the new object
                HttpEntity result = response.getEntity();
                return Util.getHandle( result );
            }
        } catch ( XPathExpressionException e2) {
        		e2.printStackTrace();
        }
        finally {
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
    

    public static void main( String[] args ) throws Exception {
        final Random random = new Random();
        
        // Append random number to title to make it unique
        String docTitle = "Test Document "
                + new BigInteger( 130, random ).toString( 32 );

        CreateDocument test = new CreateDocument();
        
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();

        // Upload to Folder-61 "Top Level Folder C". Document will be
        // owned by User-537
        int newDocHnd = test.uploadDocument( 61, docTitle,
                Paths.get( "/Users/fmasri/Desktop/PARC/Chat/RequestForProposal/RFPvsProposals/Architecture/RFP.pdf" ), 537 );
        System.out.println( "New Document Handle: " + newDocHnd );

        test.httpclient.close();
    }

}
