package com.projectbronze.pbbot.utils;

import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.data.AdvUser;
import com.projectbronze.pbbot.config.BotConfig;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;

public class LevelUtils
{
	public static boolean isFullAdmin(User usr)
	{
		return canUse(usr, 100);
	}

	public static boolean canUse(User user, Command cm)
	{
		return canUse(user, cm.level);
	}
	
	public static boolean canUse(User user, int level)
	{
		return getLevel(user) >= level;
	}

	public static int getLevel(User usr)
	{
		return AdvUser.of(usr).getLevel();
	}

	public static void addAdmin(User usr, int level)
	{
		AdvUser.of(usr).setLevel(level);
	}

	public static void removeAdmin(User usr)
	{
		AdvUser.of(usr).setLevel(0);
	}

	public static void block(User usr)
	{
		AdvUser.of(usr).setLevel(-1);
	}

	public static void unblock(User usr)
	{
		removeAdmin(usr);//Why not?
	}

	public static void addDefaultAdmins(JDA bot)
	{
		for(String adm : BotConfig.DEFAULT_ADMINS)
		{
			addAdmin(bot.getUserById(adm), 100);
		}
	}
}
