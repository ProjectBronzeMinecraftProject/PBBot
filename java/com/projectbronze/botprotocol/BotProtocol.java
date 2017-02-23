package com.projectbronze.botprotocol;

import java.util.HashMap;
import java.util.List;

import com.gt22.jdaenchacer.getters.Getters;
import com.gt22.jdaenchacer.getters.Wrapper;
import com.projectbronze.botprotocol.BotProtocol.Command.CommandAction;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.LevelUtils;

import net.dv8tion.jda.core.entities.User;


public class BotProtocol {
	
	public static final String DIVIDER = ":#:", OK = "ok", INVALID_CMD = "nocommand";
	public static class Command {
		
		public static interface CommandAction {
			String perform(String name, List<String> args) throws IllegalArgumentException;
		}
		
		public final String name;
		public final CommandAction action;
		public Command(String name, CommandAction action) {
			this.name = name;
			this.action = action;
		}
		
		public String form(Object... args) {
			String ret = name;
			for(Object a : args) {
				ret += DIVIDER + a;
			}
			return ret;
		}
		
	}
	private static final HashMap<String, Command> COMMANDS = new HashMap<>();
	//Not enough args command for quick access
	public static Command NEA;
	private static String lastCode;
	public static void init() {
		NEA = addCommand("notEnoughArgs", (cmd, args) -> "");
		addCommand("OK", (cmd, args) -> "");
		addCommand("connect", (msg, args) -> {
			validateArgs(args, 2);
			User client = Core.bot.getUserById(args.get(0));
			client.getPrivateChannel().sendMessage("twoauth" + DIVIDER + "code" + DIVIDER + createConnectCode());
			return OK;
		});
		addCommand("connectCode", (cmd, args) -> {
			validateArgs(args, 1);
			if(lastCode == null || !args.get(0).equals(lastCode)) {
				closeSocket();
			}
			lastCode = null;
			return "";
		});
		addCommand("print", (cmd, args) -> {
			validateArgs(args, 1);
			System.out.println(args.get(0));
			return OK;
		});
		addCommand("askClass", (cmd, args) -> {
			validateArgs(args, 1);
			Wrapper<User> user = Getters.getUser(args.get(0), Core.bot);
			return user.single.map(LevelUtils::getLevel).map(l -> l == 0 ? "NOCLASS" : "FOUND" + DIVIDER + l).orElse("UNNKNOWN" + DIVIDER + user.state);
		});
	}
	
	private static void validateArgs(List<String> args, int min) {
		if(args.size() < min) {
			throw new IllegalArgumentException(min + "");
		}
	}
	
	public static Command getCommand(String cmd) {
		return COMMANDS.get(cmd);
	}
	
	static Command addCommand(String name, CommandAction action) {
		return COMMANDS.put(name, new Command(name, action));
	}
	
	private static String createConnectCode() {
		
		return lastCode = createCodePart() + createCodePart() + createCodePart() + createCodePart();
	}
	
	private static String createCodePart() {
		return Core.rand.nextInt(10) + 1 + "";
	}
	
	private static void closeSocket() {
		try {
			Server.handler.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
