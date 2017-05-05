package com.parc.troy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.parc.xi.dm.Console;
import com.parc.xi.dm.Config;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TroyConsole {
	
	private static Logger LOGGER = Logger.getLogger(TroyConsole.class.getName());
	
	public static void main (String[] args) throws IOException{
		OttoInterface.initialize();
		LOGGER.info("Initialized Otto interface.");
		Config.setProperty("Console.dm", "DM.troy");
		LOGGER.info("Starting new otto console.");
		Console console = new Console();
		console.run(true);
		
	}

}
