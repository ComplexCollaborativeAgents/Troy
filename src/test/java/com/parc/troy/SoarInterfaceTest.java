package com.parc.troy;

import java.net.URISyntaxException;

import org.junit.Test;

import junit.framework.TestCase;

public class SoarInterfaceTest extends TestCase {

    @Test
    public void testGetFullPath() throws URISyntaxException {
        String fullPath = SoarInterface.getFullPath("classpath:/sample.properties");
        assertTrue(fullPath.endsWith("target/test-classes/sample.properties"));
    }

    @Test
    public void testGetFullPathNull() throws URISyntaxException {
        String fullPath = SoarInterface.getFullPath(null);
        assertNull(fullPath);
    }

}
