package com.projectbronze.pbbot;

import com.gt22.jdaenchacer.command.CommandManager;
import com.projectbronze.botlauncher.api.IBot;
import com.projectbronze.pbbot.command.Commands;
import com.projectbronze.pbbot.console.ConsoleReader;
import com.projectbronze.pbbot.data.AdvGuild;
import com.projectbronze.pbbot.data.AdvUser;
import com.projectbronze.pbbot.log.LogStream;
import com.projectbronze.pbbot.nohub.NohubConst;
import com.projectbronze.pbbot.utils.Constants;
import com.projectbronze.pbbot.utils.LevelUtils;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.AnnotatedEventManager;
import net.dv8tion.jda.hooks.SubscribeEvent;

public class Core implements IBot
{
	public static JDA bot;
	public static boolean debug = false;
	public static LogStream log, err;
	public static CommandManager commands = new CommandManager(LevelUtils::canUse, "!");
	public static void main(String[] args)
	{
		processArgs(args);
		try
		{
			err = new LogStream(System.err);
			log = new LogStream(System.out);
			bot = new JDABuilder().setBotToken(NohubConst.TOKEN).setEventManager(new AnnotatedEventManager()).addListener(new Core()).setBulkDeleteSplittingEnabled(false).buildBlocking();
		}
		catch (Exception e)
		{
			e.printStackTrace(err != null && err.canLog() ? err : System.err);
			exit(Constants.EXIT_EXCEPTION, e);
		}
		Commands.init();
		ShutdonHook.init();
	}

	private static void processArgs(String[] args)
	{
		for (String s : args)
		{
			switch (s)
			{
				case ("--debug"):
				case ("-d"):
				{
					debug = true;
					break;
				}
			}
		}
	}

	public static void exit(int code, Throwable cause)
	{
		ShutdonHook.exit(code, cause);
	}
	
	public static void reply(MessageChannel cn, User sender, String text)
	{
		cn.sendMessage(sender.getAsMention() + " " + text);
	}

	public static void reply(Message orig, String reply)
	{
		reply(orig.getChannel(), orig.getAuthor(), reply);
	}

	public static void reply(Message orig, String reply, Object... format)
	{
		reply(orig, String.format(reply, format));
	}
	
	@SubscribeEvent
	public void ready(ReadyEvent e)
	{
		JDA bot = e.getJDA();
		Commands.init();
		LevelUtils.addDefaultAdmins(bot);
		ConsoleReader.init();
		bot.getVoiceChannels().parallelStream().filter(ch -> ch.getUsers().parallelStream().anyMatch(LevelUtils::isFullAdmin)).findFirst().ifPresent(chan -> chan.getGuild().getAudioManager().openAudioConnection(chan));
		bot.getAccountManager().setGame("!команды");
	}
	
	public static final String botMention = "!";
	@SubscribeEvent
	public void msg(MessageReceivedEvent e)
	{
		try {
			Message msg = e.getMessage();
			AdvGuild guild = AdvGuild.of(e.getGuild());
			User usr = msg.getAuthor();
			String text = msg.getContent();
			if(LevelUtils.getLevel(usr) < 0)
			{
				msg.deleteMessage();
				return;
			}
			if(guild.isBotonlyChannel(msg.getChannelId()) && msg.getAuthor() != Core.bot.getSelfInfo())
			{
				msg.deleteMessage();
			}
			if(guild.hasWhitelistedChannels() && !guild.isWhitelistedChannel(msg.getChannelId()) && !guild.isBotonlyChannel(msg.getChannelId()) || guild.isBlacklistedChannel(msg.getChannelId()))
			{
				return;
			}
			checkIfDepartured(msg, usr, guild.base);
			if (text.startsWith(botMention)) {
				commands.tryExecuteCommand(text, msg, usr, guild.base);
			}
		} catch (Exception ex) {
			handleCriticalError(e.getMessage(), ex);
		}
	}
	
	private static void checkIfDepartured(Message msg, User usr, Guild guild)
	{
		AdvUser uesr = AdvUser.of(usr);
		if (uesr.hasData("dep") && uesr.getTag("dep").getString("from").equals(guild.getId())) {
			Core.reply(msg, "С возвращением");
			uesr.removeData("dep");
			guild.getManager().unmute(usr);
		}
	}
	
	private static void handleCriticalError(Message msg, Throwable ex)
	{
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

	@Override
	public void start()
	{
		main(new String[]{});
	}

	@Override
	public JDA getBot()
	{
		return bot;
	}
}