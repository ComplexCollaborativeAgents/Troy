package com.parc.troy;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigHelperTests extends TestCase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetFullPath() throws URISyntaxException, FileNotFoundException {
        String fullPath = ConfigHelper.getFullFilePath("src/test/resources/sample.properties");
        assertEquals(fullPath, "src/test/resources/sample.properties");
    }

    @Test
    public void testGetFullPathClasspath() throws URISyntaxException, FileNotFoundException {
        String fullPath = ConfigHelper.getFullFilePath("classpath:/sample.properties");
        assertTrue(fullPath.endsWith("target/test-classes/sample.properties"));
    }

    @Test
    public void testGetFullPathNull() throws URISyntaxException, FileNotFoundException {
        String fullPath = ConfigHelper.getFullFilePath(null);
        assertNull(fullPath);
    }

    @Test
    public void testGetFullPathEmpty() {
        try {
            ConfigHelper.getFullFilePath("");
        } catch (Exception e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }

    @Test
    public void testGetFullPathInvalid() {
        try {
            ConfigHelper.getFullFilePath("classpath::::/path");
        } catch (Exception e) {
            assertTrue(e instanceof URISyntaxException);
        }
    }

}
