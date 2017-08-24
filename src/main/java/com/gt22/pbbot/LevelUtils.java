package com.gt22.pbbot;

import com.gt22.pbbot.discord.misc.PermissionedCommand;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import net.dv8tion.jda.core.entities.User;

public class LevelUtils {

	public static boolean canUse(PermissionedCommand cmd, User user) {
		return canUse(cmd, Users.of(user));
	}

	public static boolean canUse(PermissionedCommand cmd, TMBotUser user) {
		return user.getLevel() >= cmd.getRequiredPermission();
	}

}
