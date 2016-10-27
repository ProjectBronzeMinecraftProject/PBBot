package com.projectbronze.pbbot.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.gt22.jdaenchacer.command.Command;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.data.AdvUser;
import com.projectbronze.pbbot.music.MusicHandler;
import com.projectbronze.pbbot.utils.comporator.FileComporatorDir;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;

public class FormatUtils
{

	public static String formatHelp()
	{
		String ret = "";
		List<Command> commands = Core.commands.getCommands();
		for (Command cm : commands)
		{
			ret += String.format("• %s,\n", cm.name);
		}
		return String.format("Команды:\n%s", ret);
	}

	public static String formatHelp(Command cm)
	{
		return String.format(
				  "Комманда %s:\n"
				+ "Короткое имя: %s\n"
				+ "%s\n"
				+ "Синтаксис: %s\n"
				+ "Требуемый уровень: %s\n",
				cm.name, cm.shortname, cm.desc, cm.syntax, cm.level);
	}

	public static String formatDir(File dir, boolean addDirName, int indent)
	{
		String ret = "";
		if (addDirName)
		{
			ret += getIndent(indent - 1) + dir.getName() + ": {\n";
		}
		List<File> files = Arrays.asList(dir.listFiles());
		files.sort(FileComporatorDir.instance);
		for (int i = 0; i < files.size(); i++)
		{
			File file = files.get(i);
			if (file.isFile())
			{
				ret += (addDirName ? getIndent(indent) : "") + file.getName().substring(0, file.getName().lastIndexOf('.')) + "\n";
			}
			else
			{
				ret += (addDirName ? (getIndent(indent) + "\n") : "\n") + formatDir(file, true, indent + 1) + getIndent(indent) + "}\n";
			}
		}

		return ret;
	}

	private static String getIndent(int indent)
	{
		String ret = "";
		for (int i = 0; i < indent; i++)
		{
			ret += "\t";
		}
		return ret;
	}

	public static String formatSong(int pos)
	{
		Object play = MusicHandler.playlistGet(pos);
		String name = play instanceof File ? ((File) play).getPath() : play instanceof URL ? play.toString() : "Cannot read";
		return (pos + 1) + ": " + (play instanceof File ? name.replace('\\', '/').substring(name.indexOf('/') + 1, name.lastIndexOf('.')) : name) + '\n';
	}

	public static String formatPlaylist()
	{
		String ret = "command.format.playlist.title";
		int size = MusicHandler.playlistSize();
		for (int i = 0; i < size; i++)
		{
			ret += formatSong(i);
		}
		return ret + "```";
	}

	public static String formatAdmins()
	{
		return "```Админы:\n" + Core.bot.getUsers().parallelStream().map(AdvUser::of).filter(u -> u.getLevel() > 0).sequential().sorted((u1, u2) -> Integer.compare(u2.getLevel(), u1.getLevel())).map(u -> "• " + u.base.getUsername() + ":" + u.getLevel()).reduce((s1, s2) -> s1 + "\n" + s2).orElse("нету") + "```";
	}

	public static String fortmatBlocked()
	{
		return Core.bot.getUsers().parallelStream().map(AdvUser::of).filter(u -> u.getLevel() < 0).map(u -> "• " + u.base.getUsername()).reduce((s1, s2) -> s1 + "\n" + s2).orElse("Нету");
	}

	public static String formatUser(User usr, Guild guild)
	{
		String nick = guild.getNicknameForUser(usr);
		int lvl = LevelUtils.getLevel(usr);
		return String.format(
				  "Информация о: %s"
				+ "Псевдоним: %s"
				+ "Статус: %s"
				+ "Определитель: %s"
				+ "Статус: %s",
				usr.getUsername(), nick == null ? "Отсутствует" : nick, (lvl < 0 ? "Заблокированый" : lvl == 0 ? "Пользователь" : "Админ#" + lvl), usr.getId(), usr.getDiscriminator(), usr.getOnlineStatus());
	}

	public static String formatBash(Random r)
	{
		int count = BashUtils.getQuotesCount();
		while (true)
		{
			int quotePos = r.nextInt(count) + 1;
			URL bash = BashUtils.getQuoute(quotePos);
			try
			{
				URLConnection conn = bash.openConnection();
				BufferedReader b = new BufferedReader(new InputStreamReader(conn.getInputStream(), "windows-1251"));
				if (conn.getURL().getPath() != bash.getPath())
				{
					b.close();
					continue;
				}
				String quote = BashUtils.findQuote(b);
				return "```#" + quotePos + "-\n" + formatHtmlChars(quote) + "```";
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		}
		return "Что-то пошло не так";
	}

	public static String formatHtmlChars(String s)
	{
		return s.replace("&amp;", "").replaceAll("(<br>|<br />)", "\n").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replaceAll("&#039;", "'");
	}

	

}
