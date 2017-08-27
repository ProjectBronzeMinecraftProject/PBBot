package com.gt22.pbbot.discord;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.commands.*;
import com.gt22.pbbot.interfaces.ITMBModule;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.jagrosh.jdautilities.commandclient.CommandClient;
import com.jagrosh.jdautilities.commandclient.CommandClientBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;

public class DiscordCore implements ITMBModule {

	static JDA bot;
	private static CommandClient commands;
	private static SimpleLog LOG = SimpleLog.getLog("TMBot#Discord");

	public void init() throws LoginException, RateLimitedException, InterruptedException {
		LOG.setLevel(SimpleLog.Level.ALL);
		buildCommandClient();
		buildBot();
	}

	private void buildCommandClient() {
		commands = new CommandClientBuilder()
			.setOwnerId(Core.getConfig().OWNER)
			.setCoOwnerIds(Core.getConfig().CO_OWNERS)
			.setGame(Game.of("Type 'sudo auth'"))
			.setPrefix("sudo ")
			.addCommands(new SystemCommands().init())
			.addCommands(new FunCommands().init())
			.addCommands(new QuoteCommands().init())
			.addCommand(new AuthCommand())
			.addCommands(new ClassificationCommands().init())
			.addCommands(new MusicCommands().init())
			.useHelpBuilder(false)
			.setEmojis("<:asset:230288765724131329>", "<:irrelevant_threat:340157734248775680>", "<:relevant_threat:340157956286840844>")
			.build();
	}

	private void buildBot() throws LoginException, RateLimitedException, InterruptedException {
		new JDABuilder(AccountType.BOT)
			.setStatus(OnlineStatus.DO_NOT_DISTURB)
			.setGame(Game.of("Loading..."))
			.setToken(Core.getConfig().TOKEN)
			.addEventListener(commands)
			.addEventListener(new DiscordEventHandler())
			.setBulkDeleteSplittingEnabled(false)
			.buildBlocking();
	}

	@Override
	public String name() {
		return "DiscordInterface";
	}

	@Override
	public boolean isReloadable() {
		return false;
	}

	@Override
	public void reload() {
		//NO-OP
	}

	public static void contactAdmin(MessageEmbed embed) {
		TMBotUser admin = Users.of("admin");
		if (admin != null && admin.getDiscordUser() != null) {
			admin.getDiscordUser().openPrivateChannel().queue(ch -> ch.sendMessage(embed).queue());
		}
	}

	public static JDA getBot() {
		return bot;
	}

	public static CommandClient getCommands() {
		return commands;
	}

	public static SimpleLog getDiscordLog() {
		return LOG;
	}
}
