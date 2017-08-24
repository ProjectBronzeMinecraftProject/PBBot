package com.gt22.pbbot.discord.commands.utils;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

public interface ICommandAction {

	void execute(CommandEvent e) throws Throwable;
}
