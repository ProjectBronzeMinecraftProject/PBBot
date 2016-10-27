package com.projectbronze.pbbot.console;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.Constants;

public class ConsoleReader
{
	public static void init()
	{
		new Thread(() ->
		{
			ConsoleCommands.init();
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (true)
			{
				try
				{
					String line = console.readLine();
					if (line == null)
					{
						Core.log.info("Unable to read console, reader stopping");
						return;
					}
					Core.log.info("Input: " + line);
					if (line.equals("stop"))
					{
						Core.exit(Constants.EXIT_NORMAL, null);
					}
					else
					{
						ConsoleCommandManager.tryExecuteCommand(line);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
		;
	}
}
