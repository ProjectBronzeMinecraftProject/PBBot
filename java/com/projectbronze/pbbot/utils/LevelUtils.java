package com.projectbronze.pbbot.utils;

import com.gt22.jdaenchacer.command.Command;
import com.projectbronze.pbbot.data.AdvUser;
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
		addAdmin(bot.getUserById("123471451750793219"), 100);
		addAdmin(bot.getUserById("146275563022188544"), 100);
		addAdmin(bot.getUserById("222052803013509131"), 100);
	}
}
