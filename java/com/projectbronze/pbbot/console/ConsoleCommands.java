package com.projectbronze.pbbot.console;

import static com.projectbronze.pbbot.console.ConsoleCommandManager.addCommand;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.LevelUtils;

public class ConsoleCommands
{
	public static void init()
	{
		addCommand("АдминРесет", "адмрс", (args) ->
		{
			LevelUtils.addDefaultAdmins(Core.bot);
			Core.log.info("Админы перезагружены");
		});
	}
}
