package com.parc.troy;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class ConfigHelper {

	/**
	 * Get the filename path for the specified resource value. Resource values might contain a classpath
	 * prefix indicating they should be found by searching the current classpath. Whether the value is
	 * specified as a classpath or absolute path, this method returns the absolute path name to the resource.
	 * @param resourceValue the value specified as a property value that might have the classpath prefix
	 * @return the full path to the specified resource or null if resourceValue is null
	 * @throws URISyntaxException if the file path is invalid
	 * @throws FileNotFoundException if the calculated file path is not a file or not readable
	 */
	public static String getFullFilePath(String resourceValue) throws URISyntaxException, FileNotFoundException {
	    final String CP_PREFIX = "classpath:";
	    String fullPathString = null;
        File soarRulesFile = null;

        if (resourceValue == null) return null;

        if (resourceValue.startsWith(CP_PREFIX)) {
        	System.out.println("resource value is " + resourceValue.substring(CP_PREFIX.length()));
            URL resourcePath = ConfigHelper.class.getResource(resourceValue.substring(CP_PREFIX.length()));
            if (resourcePath == null) throw new URISyntaxException(resourceValue, "Invalid path to specified resource");
            soarRulesFile = Paths.get(resourcePath.toURI()).toFile();
            resourceValue = soarRulesFile.getAbsoluteFile().toString();
        } else {
            soarRulesFile = Paths.get(resourceValue).toFile();
        }
        if (!soarRulesFile.canRead() && !soarRulesFile.isDirectory()) throw new FileNotFoundException("Cannot find folder or read file: " + resourceValue);
       
        fullPathString = soarRulesFile.toString();
        return fullPathString;
	}
}
