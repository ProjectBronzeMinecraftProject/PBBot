package com.gt22.pbbot.discord.commands;

import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.user.Classification;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.io.IOException;

public class AuthCommand extends Command {

	public AuthCommand() {
		name = "auth";
		guildOnly = false;
		ownerCommand = false;
		help = "Authenticate user";
		category = ClassificationCommands.cat;
		arguments = "%name%";
	}

	@Override
	protected void execute(CommandEvent e) {
		switch (Users.auth(e.getAuthor(), e.getArgs().isEmpty() ? e.getAuthor().getName() : e.getArgs())) {
			case SUCCESS: {
				TMBotUser user = Users.of(e.getAuthor());
				e.reply(EmbedUtils.create(0x00FF00, "Success", "Authentication complete. Hello " + user.getName() + ".\nYou can now type 'sudo help' to view commands", user.getClassification().getImg()));
				e.reactSuccess();
				break;
			}
			case RESERVED_NAME: {
				e.reply(EmbedUtils.create(0xFF0000, "Name is reserved", "This name is reserved for special user", Classification.RELEVANT_THREAT.getImg()));
				e.reactWarning();
				break;
			}
			case NAME_ALREADY_AUTHENTICATED: {
				e.reply(EmbedUtils.create(0xFF0000, "Already authenticated", "User with this name is already authenticated", Classification.IRRELEVANT_THREAT.getImg()));
				e.reactWarning();
				break;
			}
			case USER_ALREADY_AUTHENTICATED: {
				TMBotUser user = Users.of(e.getAuthor());
				try {
					e.reply(EmbedUtils.create(0xFF0000, "Already authenticated", "You are already authenticated, " + user.getName() + ".\nType 'sudo help' to view all commands", user.getAvatarWithClassUrl().get()));
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
				e.reactWarning();
				break;
			}
		}
	}
}
