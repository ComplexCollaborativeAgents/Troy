package com.parc.troy;


import java.io.IOException;


import com.parc.xi.dm.Console;
import com.parc.xi.dm.Config;


import org.apache.log4j.Logger;


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
