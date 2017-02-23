package com.projectbronze.pbbot;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gt22.jdaenchacer.MessageHelper;
import com.gt22.jdaenchacer.command.CommandManager;
import com.gt22.jdaenchacer.command.CommandManager.ExecuteResult;
import com.gt22.jdaenchacer.data.AdvGuild;
import com.gt22.jdaenchacer.data.AdvUser;
import com.projectbronze.pbbot.command.Commands;
import com.projectbronze.pbbot.config.BotConfig;
import com.projectbronze.pbbot.log.LogStream;
import com.projectbronze.pbbot.music.MusicHandler;
import com.projectbronze.pbbot.utils.Constants;
import com.projectbronze.pbbot.utils.LevelUtils;
import com.projectbronze.pbbot.utils.MiscUtils;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import net.dv8tion.jda.core.managers.AudioManager;


public class Core/* implements IBot*/ {
	public static JDA bot;
	public static boolean debug = false, fileLogs = false, consoleLogs = false;
	public static PrintStream info;
	public static LogStream log, err;
	public static final String botMention = "!";
	public static CommandManager commands = new CommandManager(LevelUtils::canUse, botMention, (cm, args, sender) -> info.println(String.format("Performing command %s with args %s", cm.name, Arrays.toString(args))));
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final Random rand = new Random();

	public static void main(String[] args) {
		processArgs(args);
		try {
			if (info == null) {
				info = System.out;
			}
			err = new LogStream(System.err);
			log = new LogStream(info);
			try {
				System.setOut(log);
				System.setErr(err);
			} catch (SecurityException e) {
				log.error("Unable to set output streams");
			}
			info.println("Starting bot");
			info.flush();
			bot = new JDABuilder(AccountType.BOT).setToken(BotConfig.TOKEN).setEventManager(new AnnotatedEventManager()).addListener(new Core()).setBulkDeleteSplittingEnabled(false).buildAsync();
		} catch (Exception e) {
			e.printStackTrace(err != null && err.canLog() ? err : System.err);
			exit(Constants.EXIT_EXCEPTION, e);
		}
		Commands.init();
		ShutdonHook.init();
	}

	private static void processArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			switch (s) {
				case ("--debug"):
				case ("-d"): {
					debug = true;
					break;
				}
				case ("--use-file-logs"):
				case ("-ufl"): {
					fileLogs = true;
					break;
				}
				case ("--use-console-logs"):
				case ("-ucl"): {
					consoleLogs = true;
					break;
				}
				case ("--music-dir"):
				case ("-md"): {
					MusicHandler.musicDir = new File(args[++i]);
					System.out.println(MusicHandler.musicDir);
					if (!MusicHandler.musicDir.exists()) {
						System.out.println("Unable to find music dir");
						System.exit(1);
					}
					break;
				}
			}
		}
		if (MusicHandler.musicDir == null) {
			MusicHandler.musicDir = new File("Music");
		}
	}

	public static void exit(int code, Throwable cause) {
		ShutdonHook.exit(code, cause);
	}

	public static void reply(Message orig, String reply) {
		MessageHelper.reply(orig, reply);
	}

	public static void reply(Message orig, String reply, Object... format) {
		reply(orig, String.format(reply, format));
	}

	@SubscribeEvent
	public void ready(ReadyEvent e) {
		JDA bot = e.getJDA();
		Commands.init();
		log.info("Commands added");
		LevelUtils.addDefaultAdmins(bot);
		log.info("Default admins added");
		// ConsoleReader.init();
		bot.getVoiceChannels().parallelStream().filter(ch -> ch.getMembers().parallelStream().map(Member::getUser).anyMatch(LevelUtils::isFullAdmin)).findFirst().ifPresent(ch -> {
			AudioManager m = ch.getGuild().getAudioManager();
			if (m.isAttemptingToConnect() || m.isConnected()) {
				m.closeAudioConnection();
			}
			m.openAudioConnection(ch);
			log.info(String.format("Found admin in channel %s:%s, connecting", ch.getGuild().getName(), ch.getName()));
		});
		MiscUtils.setGame("!команды");
		log.info("Bot launched");
		//Server.init();
	}

	@SubscribeEvent
	public void msg(MessageReceivedEvent e) {
		try {
			Message msg = e.getMessage();
			User usr = msg.getAuthor();
			String text = msg.getContent();
			if (usr == null) {
				return;// Webhooks
			}
			if (LevelUtils.getLevel(usr) < 0) {
				msg.deleteMessage();
				return;
			}
			Guild g = e.getGuild();
			if (g != null) {
				AdvGuild guild = AdvGuild.of(e.getGuild());
				if (guild.isBotonlyChannel(msg.getChannel().getId()) && msg.getAuthor() != Core.bot.getSelfUser()) {
					msg.deleteMessage();
				}
				if (guild.hasWhitelistedChannels() && !guild.isWhitelistedChannel(msg.getChannel().getId()) && !guild.isBotonlyChannel(msg.getChannel().getId()) || guild.isBlacklistedChannel(msg.getChannel().getId())) {
					return;
				}
				checkIfDepartured(msg, guild.base.getMember(usr), guild.base);
			}
			if (text.startsWith(botMention)) {
				if (commands.tryExecuteCommand(text, msg, usr, g) == ExecuteResult.NO_SUCH_COMMNAD) {
					reply(msg, "Не вижу у себя в оперативке такой комманды");
				}
			}
		} catch (Exception ex) {
			handleCriticalError(e.getMessage(), ex);
		}
	}

	private static void checkIfDepartured(Message msg, Member usr, Guild guild) {
		AdvUser uesr = AdvUser.of(usr.getUser());
		if (uesr.hasData("dep") && uesr.getTag("dep").getString("from").equals(guild.getId())) {
			Core.reply(msg, "С возвращением");
			uesr.removeData("dep");
			guild.getController().setMute(usr, false);
		}
	}

	private static void handleCriticalError(Message msg, Throwable ex) {
		try {
			Core.reply(msg, "Что-то пошло совсем не так, напишите автору.");
			ex.printStackTrace(Core.err);
		} catch (Exception ex2) {
			Core.err.warning("Unable to send error message\nMain Exception:");
			ex.printStackTrace(Core.err);
			Core.err.warning("Sending exception:");
			ex2.printStackTrace(Core.err);
		}
	}

	/*@Override
	public void start(PrintStream infoStream) {
		info = infoStream;
		main(new String[] {});
	}

	@Override
	public JDA getBot() {
		return bot;
	}*/
}
