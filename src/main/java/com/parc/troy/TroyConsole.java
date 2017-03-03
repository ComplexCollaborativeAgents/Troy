package com.parc.troy;

import java.io.IOException;

import com.parc.xi.dm.Config;
import com.parc.xi.dm.Console;

public class TroyConsole {

	public static void main(String[] args) throws IOException {
		OttoInterface.initialize();
		Config.setProperty("Console.dm", "DM.troy");
		Console console = new Console();
		console.run(false);
	}

}
