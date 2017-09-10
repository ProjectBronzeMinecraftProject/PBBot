package com.gt22.pbbot.discord;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.commands.*;
import com.gt22.pbbot.discord.music.MusicHandler;
import com.gt22.pbbot.interfaces.ITMBModule;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.gt22.randomutils.utils.JoinUtils;
import com.jagrosh.jdautilities.commandclient.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiscordCore implements ITMBModule {

	static JDA bot;
	private static CommandClient commands;
	private static SimpleLog LOG = SimpleLog.getLog("TMBot#Discord");
	private static final String PREFIX = "sudo ";

	public void init() throws LoginException, RateLimitedException, InterruptedException {
		LOG.setLevel(SimpleLog.Level.ALL);
		buildCommandClient();
		buildBot();
	}

	private void buildCommandClient() {
		commands = new CommandClientBuilder()
			.setOwnerId(Core.getConfig().OWNER)
			.setCoOwnerIds(Core.getConfig().CO_OWNERS)
			.setGame(Game.of("Type '" + PREFIX + "auth'"))
			.setPrefix(PREFIX)
			.addCommands(new SystemCommands().init())
			.addCommands(new FunCommands().init())
			.addCommands(new QuoteCommands().init())
			.addCommand(new AuthCommand())
			.addCommands(new ClassificationCommands().init())
			.addCommands(new MusicCommands().init())
			.useHelpBuilder(false)
			.setListener(new CommandListener() {
				@Override
				public void onCommand(CommandEvent event, Command command) {
				}

				@Override
				public void onCompletedCommand(CommandEvent event, Command command) {
				}

				@Override
				public void onTerminatedCommand(CommandEvent event, Command command) {
				}

				@Override
				public void onNonCommandMessage(MessageReceivedEvent event) {
					try {
						handleNonCommandFile(event);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			})
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

	private void handleNonCommandFile(MessageReceivedEvent e) throws IOException {
		String text = e.getMessage().getContent();
		if(text.startsWith(PREFIX)) {
			String name = text.substring(PREFIX.length());
			Path p;
			if (Files.exists(p = FunCommands.VOICE_DIR.resolve(name + ".mp3"))) { //Sound
				MusicHandler.add(p, e.getGuild(), false, false);
			} else if (Files.exists(p = Paths.get("msgs", name + ".msg"))) { //Constant message
				String msg = Files.lines(p).reduce(JoinUtils.join("\n")).orElse("EMPTY_MESSAGE");
				if (e.isFromType(ChannelType.PRIVATE) || e.getTextChannel().canTalk()) {
					e.getTextChannel().sendMessage(msg).queue();
				}
			}
		}
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
