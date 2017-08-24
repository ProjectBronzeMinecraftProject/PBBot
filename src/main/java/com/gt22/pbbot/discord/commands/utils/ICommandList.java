package com.gt22.pbbot.discord.commands.utils;

import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.utils.ManualFuture;
import com.gt22.randomutils.Instances;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedConsumer;

import java.awt.*;
import java.util.function.Consumer;

public interface ICommandList {

	AdvancedCategory getCategory();
	Command[] init();

	default CommandBuilder command(String name, String help, ICommandAction action) {
		return new CommandBuilder().setName(name).setHelp(help).setAction(action).setCategory(getCategory());
	}

	default void reply(CommandEvent e, Color color, String title, String message, String image) {
		Instances.getExecutor().submit(() -> e.reply(EmbedUtils.create(color, title, message, image)));
	}

	default void reply(CommandEvent e, int color, String title, String message, String image) {
		Instances.getExecutor().submit(() -> e.reply(EmbedUtils.create(new Color(color), title, message, image)));
	}

	default void reply(CommandEvent e, Color color, String title, String message, ManualFuture<String> image) {
		try {
			reply(e, color, title, message, image.get());
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	default void reply(CommandEvent e, int color, String title, String message, ManualFuture<String> image) {
		reply(e, new Color(color), title, message, image);
	}

	default void reply(CommandEvent e, Color color, String title, String message) {
		reply(e, color, title, message, (String) null);
	}

	default void reply(CommandEvent e, int color, String title, String message) {
		reply(e, new Color(color), title, message, (String) null);
	}

}
