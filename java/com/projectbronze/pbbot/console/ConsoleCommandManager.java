package com.projectbronze.pbbot.console;

import java.util.ArrayList;
import java.util.List;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.console.ConsoleCommandManager.ConsoleCommand.ConsoleCommandAction;

public class ConsoleCommandManager {

	public static class ConsoleCommand {

		public static interface ConsoleCommandAction {
			public void performCommand(String[] args);
		}

		public String name, shortname;
		public ConsoleCommandAction action;
		public int level;

		public ConsoleCommand(String name, String shortname, ConsoleCommandAction action) {
			this.name = name;
			this.shortname = shortname;
			this.action = action;
		}

	}

	private static List<ConsoleCommand> commands = new ArrayList<ConsoleCommand>();

	public static void addCommand(ConsoleCommand cm) {
		Core.log.debug("Adding command: " + cm.name);
		commands.add(cm);
	}

	public static void addCommand(String name, String shortname, ConsoleCommandAction action) {
		addCommand(new ConsoleCommand(name, shortname, action));
	}

	public static ConsoleCommand getCommandByName(String name) {
		for (ConsoleCommand cm : commands) {
			if (cm.name.equals(name) || cm.shortname.equals(name)) {
				return cm;
			}
		}
		return null;
	}

	public static List<ConsoleCommand> getCommands() {
		return commands;
	}

	public static void reset() {
		commands.clear();
		ConsoleCommands.init();
	}

	public static void tryExecuteCommand(String text) {
		String[] t = text.split("\\s", 2);
		String name = t[0].toLowerCase();
		String[] args = t.length == 1 ? new String[] {} : t[1].split("\\s");
		ConsoleCommand cm = getCommandByName(name);
		if (cm == null) {
			Core.log.info("Неизвестная консольная команда");
		} else {
			cm.action.performCommand(args);
		}
	}
}
