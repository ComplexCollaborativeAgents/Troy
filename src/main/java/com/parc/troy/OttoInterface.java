package com.parc.troy;

import java.util.logging.LogManager;

import com.parc.xi.dm.ComponentFactory;
import com.parc.xi.dm.Config;

public class OttoInterface {
	
	public static void initialize(){
		ComponentFactory.registerAgent("TroyAgent", TroyAgent.class);
		Config.addProperties("classpath:/Troy.properties");
		LogManager.getLogManager().reset();
	}

}
