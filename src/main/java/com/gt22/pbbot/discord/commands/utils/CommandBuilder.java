package com.gt22.pbbot.discord.commands.utils;

import com.gt22.pbbot.LevelUtils;
import com.gt22.pbbot.discord.DiscordCore;
import com.gt22.pbbot.discord.misc.PermissionedCommand;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.user.Classification;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;

import java.util.function.BiConsumer;

public class CommandBuilder {
	private String name = "null";
	private String help = "no help available";
	private Command.Category category = null;
	private String arguments = "";
	private boolean guildOnly = true;
	private String requiredRole = null;
	private boolean ownerCommand = false;
	private boolean hidden = false;
	private int cooldown = 0;
	private int requiredPermission = 0;
	private Permission[] userPermissions = new Permission[0];
	private Permission[] botPermissions = new Permission[0];
	private String[] aliases = new String[0];
	private Command[] children = new Command[0];
	private ICommandAction action = e -> {
		e.reply("No action specified!");
		e.reactError();
	};
	private BiConsumer<Integer, CommandEvent> onDenied = (p, e) -> {
		e.reply(EmbedUtils.create(0xFF0000, "Permission denied", "Minimum permission is " + p + ", your is " + Users.of(e.getAuthor()).getLevel(), null));
		e.reactWarning();
	};
	public CommandBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public CommandBuilder setHelp(String help) {
		this.help = help;
		return this;
	}

	public CommandBuilder setCategory(Command.Category category) {
		this.category = category;
		return this;
	}

	public CommandBuilder setArguments(String arguments) {
		this.arguments = arguments;
		return this;
	}

	public CommandBuilder setGuildOnly(boolean guildOnly) {
		this.guildOnly = guildOnly;
		return this;
	}

	public CommandBuilder setRequiredRole(String requiredRole) {
		this.requiredRole = requiredRole;
		return this;
	}

	public CommandBuilder setOwnerCommand(boolean ownerCommand) {
		this.ownerCommand = ownerCommand;
		return this;
	}

	public CommandBuilder setHidden() {
		return setHidden(true);
	}

	public CommandBuilder setHidden(boolean hidden) {
		this.hidden = hidden;
		return this;
	}

	public CommandBuilder setCooldown(int cooldown) {
		this.cooldown = cooldown;
		return this;
	}

	public CommandBuilder setRequiredPermission(int requiredPermission) {
		this.requiredPermission = requiredPermission;
		return this;
	}

	public CommandBuilder setUserPermissions(Permission... userPermissions) {
		this.userPermissions = userPermissions;
		return this;
	}

	public CommandBuilder setBotPermissions(Permission... botPermissions) {
		this.botPermissions = botPermissions;
		return this;
	}

	public CommandBuilder setAliases(String... aliases) {
		this.aliases = aliases;
		return this;
	}

	public CommandBuilder setChildren(Command... children) {
		this.children = children;
		return this;
	}

	public CommandBuilder setAction(ICommandAction action) {
		this.action = action;
		return this;
	}

	public CommandBuilder setOnDenied(BiConsumer<Integer, CommandEvent> onDenied) {
		this.onDenied = onDenied;
		return this;
	}

	public PermissionedCommand build() {
		return (new PermissionedCommand() {
			@Override
			protected void execute(CommandEvent event) {
				TMBotUser user = Users.of(event.getAuthor());
				if(user == null) {
					event.reply(EmbedUtils.create(0xFF0000, "You are not authenticated", "Run 'sudo auth %name%'. Where %name% is how do you want the bot to know you", Classification.IRRELEVANT_THREAT.getImg()));
					return;
				}
				if(LevelUtils.canUse(this, user)) {
					try {
						action.execute(event);
					} catch (Throwable throwable) {
						DiscordCore.getDiscordLog().warn(String.format("Command %s encountered an exception", CommandBuilder.this.name));
						DiscordCore.getDiscordLog().log(throwable);
					}
				} else {
					onDenied.accept(requiredPermission, event);
				}
			}

			PermissionedCommand init() {
				name               = CommandBuilder.this.name;
				help               = CommandBuilder.this.help;
				category           = CommandBuilder.this.category;
				arguments          = CommandBuilder.this.arguments;
				guildOnly          = CommandBuilder.this.guildOnly;
				requiredRole       = CommandBuilder.this.requiredRole;
				ownerCommand       = CommandBuilder.this.ownerCommand;
				isHidden           = CommandBuilder.this.hidden;
				cooldown           = CommandBuilder.this.cooldown;
				requiredPermission = CommandBuilder.this.requiredPermission;
				userPermissions    = CommandBuilder.this.userPermissions;
				botPermissions     = CommandBuilder.this.botPermissions;
				aliases            = CommandBuilder.this.aliases;
				children           = CommandBuilder.this.children;
				return this;
			}
		}).init();
	}
}
