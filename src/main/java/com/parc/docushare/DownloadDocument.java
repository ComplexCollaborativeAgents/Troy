package com.parc.docushare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

/**
 * @author fmasri
 * Class used for Downloading file from Docushare to tmp folder
 */
public class DownloadDocument {
	

	// Info needed for creating the request - taken from HttpClientLogin in order to authenticate
    private CloseableHttpClient httpclient;
    private HttpClientContext context;
    
    // Info for creating the request URL
    private static final String server = HttpClientLogin.getServer();
    private static final String shadowpath = HttpClientLogin.getShadowPath();

    public void download( int handle ) throws ClientProtocolException,
            IOException, MimeTypeException {

		HttpGet req = new HttpGet("https://" + server + shadowpath
				 + "object/" + handle + "/elf/content");

		// Send the request
		CloseableHttpResponse response = httpclient.execute(req, this.context);

        int status = response.getStatusLine().getStatusCode();
        
        if ( status >= 200 && status < 300 ) {
        	
            HttpEntity entity = response.getEntity();

            BufferedInputStream bis = new BufferedInputStream(
                    entity.getContent() );
            
            String filePath = "/Users/fmasri/Desktop/Troy/src/main/resources/tmp/download"
                    + getFileExtension( entity );

            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream( new File( filePath ) ) );
            int inByte;
            while ( (inByte = bis.read()) != -1 )
                bos.write( inByte );
            bis.close();
            bos.close();

        }
    }

    private String getFileExtension( HttpEntity entity )
            throws MimeTypeException {
    	
        ContentType contentType = ContentType.get( entity );
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType type = allTypes.forName( contentType.getMimeType() );
        String ext = type.getExtension();
        return ext;
    }


    
    
    public CloseableHttpClient getHttpClient() {
		return httpclient;
	}

	public void setHttpClient(CloseableHttpClient httpclient) {
		this.httpclient = httpclient;
	}

	public HttpClientContext getContext() {
		return context;
	}

	public void setContext(HttpClientContext context) {
		this.context = context;
	}

	public static void main( String[] args ) throws Exception {
        DownloadDocument test = new DownloadDocument();
        
        test.httpclient = HttpClientLogin.getHttpClient("username", "password");
        test.context = HttpClientLogin.getContext();

        try {
            test.download( 60 );
        } finally {
            test.httpclient.close();
        }
    }

}

