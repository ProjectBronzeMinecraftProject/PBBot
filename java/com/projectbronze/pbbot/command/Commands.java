package com.projectbronze.pbbot.command;

import com.projectbronze.pbbot.Core;

public class Commands
{
	public static void init()
	{
		Core.commands.addCommandList(new GuildCommands());
		Core.commands.addCommandList(new LevelCommands());
		Core.commands.addCommandList(new QuoteCommands());
		Core.commands.addCommandList(new SoundCommands());
		Core.commands.addCommandList(new UtilsCommands());
	}
}
