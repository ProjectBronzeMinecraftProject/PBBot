package com.gt22.pbbot.discord.commands;

import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.jagrosh.jdautilities.commandclient.Command;

import java.awt.*;

public class BashTeamCommands implements ICommandList {

	public static final AdvancedCategory cat = new AdvancedCategory("Bash.im team commands", new Color(0x777777), "https://pp.userapi.com/c411125/g12942/a_6c387bcd.jpg");

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	@Override
	public Command[] init() {
		return new Command[]{
				command("ith", "Displays ithappends.me story", e -> {
					String args = e.getArgs();
					if (args.isEmpty()) {
						//Random story

					} else if (args.matches("\\d")) {
						//Single story
					} else if (args.matches("\\d:\\d")) {
						//From:To
					} else if (args.matches("\\*\\s\\d")) {
						//X random stories count
					} else {
						reply(e, 0xFF0000, "Invalid args", "Args should be (*none*|i%story%|i%from%:i%to%|* i%count%)");
					}
				}).setArguments("(*none*|i%story%|i%from%:i%to%|* i%count%)").build()
		};
	}
}
