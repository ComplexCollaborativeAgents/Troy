package com.parc.docushare;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class Util {


    public static Document parse( HttpEntity entity ) throws ParseException,
            JDOMException, IOException {
    	
	    	SAXBuilder builder = new SAXBuilder();
	    	String xml = EntityUtils.toString( entity );
	    	
	    //System.out.println(xml);
	    	
	    	InputStream stream = new ByteArrayInputStream( xml.getBytes("UTF-8"));
	    	
        return builder.build(stream);
    }

    public static void printJDOM( Document doc ) throws IOException {
        XMLOutputter printer = new XMLOutputter( Format.getPrettyFormat() );
        System.out.println( "<----------- XML DOCUMENT ----------->" );
        printer.output( doc, System.out );
        System.out.println( "<------------------------------------>" );
    }
    
    /**
     * Retrieving the handle from HttpEntity taken from the response
     * First turn the entity to String, then to XML, then evaluate XPath and return the handle
     * @param entity HttpEntity from the response contains received data from the server - should be XML
     * @return integer - handle of the new object
     * @throws ParseException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static int getHandle( HttpEntity entity ) throws ParseException,
            IOException, XPathExpressionException {
    	
        String xml = EntityUtils.toString( entity );
        
        //System.out.println(xml);
        
        InputSource inputSource = new InputSource( new StringReader( xml ) );
        XPath xpath = XPathFactory.newInstance().newXPath();
        String handle = (String) xpath.evaluate( "/objects/object/@handle",
                inputSource, XPathConstants.STRING );
        return Integer.parseInt( handle );
    }
    
    /**
     * @param entity HttpEntity from the response contains received data from the server - should be XML
     * @return integer - handle of the root folder
     * @throws ParseException
     * @throws IOException
     * @throws XPathExpressionException
     */
    public static int getRootHandle( HttpEntity entity ) throws ParseException,
    IOException, XPathExpressionException {
    	
	String xml = EntityUtils.toString( entity );
	
	InputSource inputSource = new InputSource( new StringReader( xml ) );
	XPath xpath = XPathFactory.newInstance().newXPath();
	String handle = (String) xpath.evaluate( "/rootFolder",
	        inputSource, XPathConstants.STRING );
	return Integer.parseInt(handle);
	}
    
    public static DocushareFile getDocushareFile( HttpEntity entity ) throws ParseException,
    IOException, XPathExpressionException {
    	
	String xml = EntityUtils.toString( entity );
	
	//System.out.println(xml);
	
	InputSource inputSource = new InputSource( new StringReader( xml ) );
	XPath xpath = XPathFactory.newInstance().newXPath();
	
	Node obj = (Node) xpath.evaluate("/found/objects/object", inputSource, XPathConstants.NODE);
	
    String handle =(String) xpath.evaluate("@handle", obj, XPathConstants.STRING);
    String title =(String) xpath.evaluate("Displayable/title", obj, XPathConstants.STRING);
    String type =(String) xpath.evaluate("@classType", obj, XPathConstants.STRING);
	
	return new DocushareFile(title, handle, type);
	}
    
    
}


