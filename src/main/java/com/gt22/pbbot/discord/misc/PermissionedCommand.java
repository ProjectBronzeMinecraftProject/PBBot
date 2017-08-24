package com.gt22.pbbot.discord.misc;

import com.jagrosh.jdautilities.commandclient.Command;

public abstract class PermissionedCommand extends Command {
	protected int requiredPermission;
	protected boolean isHidden;
	public int getRequiredPermission() {
		return requiredPermission;
	}

	public boolean isHidden() {
		return isHidden;
	}
}
