package com.gt22.pbbot.discord.commands;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.DiscordCore;
import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.misc.PermissionedCommand;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.user.Classification;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.gt22.randomutils.Instances;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.jooq.lambda.Unchecked;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

public class SystemCommands implements ICommandList {
	public static final AdvancedCategory cat = new AdvancedCategory("System", new Color(0x5E5E5E), "http://www.iconsdb.com/icons/preview/gray/gear-2-xl.png");

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	public Command[] init() {
		return new Command[]{
			command("asd", "Bot joins your channel", e -> {
				e.getGuild().getAudioManager().openAudioConnection(e.getMember().getVoiceState().getChannel());
				e.reactSuccess();
			}).setRequiredPermission(30).setBotPermissions(Permission.VOICE_CONNECT).setAliases("фыв").build(),
			command("dsa", "Bot leaves your channel", e -> {
				VoiceChannel user = e.getMember().getVoiceState().getChannel();
				VoiceChannel bot = e.getSelfMember().getVoiceState().getChannel();
				if (bot != null && bot.getId().equals(user.getId())) {
					e.getGuild().getAudioManager().closeAudioConnection();
					e.reactSuccess();
				} else {
					e.reply("I'm not in your channel");
					e.reactWarning();
				}

			}).setRequiredPermission(30).setAliases("выф").build(),
			command("help", "get some help", e -> {
				if (e.getArgs().isEmpty()) {
					sendGeneralHelp(e);
				} else {
					String name = e.getArgs();
					Optional<PermissionedCommand> cmd = DiscordCore.getCommands().getCommands().parallelStream()
						.filter(c -> c instanceof PermissionedCommand)
						.map(c -> (PermissionedCommand) c)
						.filter(c -> c.getName().equals(name))
						.findAny();
					if (cmd.isPresent()) {
						PermissionedCommand c = cmd.get();
						String prefix = DiscordCore.getCommands().getPrefix();
						EmbedBuilder embed = new EmbedBuilder()
							.setTitle("Command: " + c.getName(), null)
							.setColor(((AdvancedCategory) c.getCategory()).getColor());
						createHelpForCommand(embed, c, prefix, false);
						e.reply(embed.build());
						e.reactSuccess();
					} else {
						e.reply("Command not found");
						e.reactWarning();
					}
				}
			}).setGuildOnly(false).build(),
			command("shred/system/kernel.test", "shutdowns the bot", e -> {
				TMBotUser u = Users.of(e.getAuthor());
				e.getChannel().sendMessage(EmbedUtils.create(cat.getColor().getRGB(), "Shutting down", "Goodbye, " + u.getName(), u.getAvatarWithClassUrl().get())).queue(m -> {
					DiscordCore.getBot().shutdown();
					System.exit(0);
				});
			}).setRequiredPermission(100).setOnDenied(Unchecked.biConsumer((l, e) -> {
				reply(e, 0xFF0000, "Rejecting reboot", "You have no permission to reboot this system\nContacting Admin", Users.of(e.getAuthor()).getAvatarWithClassUrl());
				e.reactError();
				TMBotUser author = Users.of(e.getAuthor());
				Instances.getExecutor().submit(Unchecked.runnable(() -> DiscordCore.contactAdmin(EmbedUtils.create(0xFFFF00, "Shutdown attempt detected", String.format("User '%s' tried to shutdown The Bot", author.getName()), author.getAvatarWithClassUrl(Classification.RELEVANT_THREAT).get()))));
			})).setAliases("shutdown").setHidden().build(),
			command("/system/protocol/hide", "changes state of hiding protocol", e -> e.reply("(enable|disable)")).setArguments("(enable|disable)").setHidden().setRequiredPermission(100).setOnDenied((l, e) -> {
				try {
					reply(e, 0xFF0000, "Protocol change blocked", "You are not admin\nContacting real admin", Users.of(e.getAuthor()).getAvatarWithClassUrl());
					e.reactError();
					TMBotUser author = Users.of(e.getAuthor());
					Instances.getExecutor().submit(Unchecked.runnable(() -> DiscordCore.contactAdmin(EmbedUtils.create(0xFFFF00,"Protocol change attempt detected", String.format("User '%s' tried to change hiding protocol of The Bot", author.getName()), author.getAvatarWithClassUrl(Classification.RELEVANT_THREAT).get()))));
				} catch (IOException e1) {
					throw new UncheckedIOException(e1);
				}
			}).setAliases("hiding").setChildren(command("disable", "disable hiding protocol", e -> {
					Core.hidingProtocol = false;
					e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
					reply(e, 0x00FF00, "Hiding deactivated", "", e.getSelfUser().getEffectiveAvatarUrl());
					e.reactSuccess();
				}).build(),
				command("enable", "enable hiding protocol", e -> {
					Core.hidingProtocol = true;
					e.getJDA().getPresence().setStatus(OnlineStatus.INVISIBLE);
					reply(e, 0x00FF00, "Hiding activated", "", e.getSelfUser().getEffectiveAvatarUrl());
					e.reactSuccess();
				}).build()).build()
		};
	}

	private void createHelpForCommand(EmbedBuilder embed, PermissionedCommand c, String prefix, boolean inline) {
		embed.addField(String.format("[%d] %s %s %s", c.getRequiredPermission(), prefix, c.getName(), c.getArguments()), c.getHelp(), inline);
		if (c.getChildren().length != 0) {
			String newPrefix = prefix + " " + c.getName();
			for (Command child : c.getChildren()) {
				createHelpForCommand(embed, (PermissionedCommand) child, newPrefix, true);
			}
		}
	}

	private void sendGeneralHelp(CommandEvent e) {
		EmbedBuilder embed = null;
		AdvancedCategory cat = null;
		String prefix = DiscordCore.getCommands().getPrefix();
		for (Command cmd : DiscordCore.getCommands().getCommands()) {
			if (cmd instanceof PermissionedCommand && (!cmd.isOwnerCommand() || e.isOwner() || e.isCoOwner()) && !((PermissionedCommand) cmd).isHidden()) {
				if (!Objects.equals(cat, cmd.getCategory())) {
					cat = (AdvancedCategory) cmd.getCategory();
					if (embed != null) {
						e.reply(embed.build());
					}
					embed = new EmbedBuilder();
					embed.setColor(cat.getColor());
					embed.setThumbnail(cat.getImg());
					embed.setTitle(cat.getName(), null);
				}
				assert embed != null; //If embed not initialized, in category init something went wrong
				embed.addField(prefix + " " + cmd.getName() + " " + cmd.getArguments(), cmd.getHelp(), false);
			}
		}
		if (embed != null) {
			e.reply(embed.build());
		}
		e.reactSuccess();
	}



}