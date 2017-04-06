package com.parc.troy;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SoarInterfaceTest extends TestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetFullPath() throws URISyntaxException, FileNotFoundException {
        String fullPath = SoarInterface.getFullPath("src/test/resources/sample.properties");
        assertEquals(fullPath, "src/test/resources/sample.properties");
    }

    @Test
    public void testGetFullPathClasspath() throws URISyntaxException, FileNotFoundException {
        String fullPath = SoarInterface.getFullPath("classpath:/sample.properties");
        assertTrue(fullPath.endsWith("target/test-classes/sample.properties"));
    }

    @Test
    public void testGetFullPathNull() throws URISyntaxException, FileNotFoundException {
        String fullPath = SoarInterface.getFullPath(null);
        assertNull(fullPath);
    }

    @Test
    public void testGetFullPathEmpty() {
        try {
            SoarInterface.getFullPath("");
        } catch (Exception e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }

    @Test
    public void testGetFullPathInvalid() {
        try {
            SoarInterface.getFullPath("classpath::::/path");
        } catch (Exception e) {
            assertTrue(e instanceof URISyntaxException);
        }
    }

}
