package com.gt22.pbbot.discord.commands;

import com.gt22.pbbot.discord.DiscordCore;
import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.getters.Getters;
import com.gt22.pbbot.getters.Wrapper;
import com.gt22.pbbot.user.Classification;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.gt22.randomutils.Instances;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import org.jooq.lambda.Unchecked;

import java.awt.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassificationCommands implements ICommandList {
	public static final AdvancedCategory cat = new AdvancedCategory("Classification", new Color(0x204020), Classification.UNKNOWN.getImg());
	private Pattern levelSet = Pattern.compile("^(.+):(\\d+)$");
	private Pattern classSet = Pattern.compile("^(.+):(.+)$");

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	@Override
	public Command[] init() {
		return new Command[]{
			command("monitor", "Displays info about user", e -> {
				TMBotUser usr = getUser(e.getArgs(), e);
				if (usr == null) {
					return;
				}
				int targetLevel = usr.getLevel();
				TMBotUser author = Users.of(e.getAuthor());
				int ownLevel = author.getLevel();
				if (targetLevel > ownLevel) {
					boolean shouldContact = usr.getName().equalsIgnoreCase("admin") || usr.getName().equalsIgnoreCase("thesystem");
					e.reply(EmbedUtils.create(0xFF0000, "No, no, no", "Monitor request denied" + (shouldContact ? "\nContacting Admin" : ""), Classification.UNKNOWN.getImg()));
					e.reactWarning();
					if(shouldContact) {
						Instances.getExecutor().submit(Unchecked.runnable(() -> DiscordCore.contactAdmin((EmbedUtils.create(0xFFFF00, "Monitor attempt detected", String.format("User '%s' tried to monitor %s", author.getName(), usr.getName()), author.getAvatarWithClassUrl(Classification.RELEVANT_THREAT).get())))));
					}
					return;
				}
				Member m = e.getGuild().getMember(usr.getDiscordUser().getBaseUser());
				Classification cls = usr.getClassification();
				Instances.getExecutor().submit(() -> {
					try {
						e.reply(new EmbedBuilder()
							.setTitle("Info about " + usr.getName(), null)
							.addField("Classification",cls.getName(), true)
							.addField("Access level", Integer.toString(usr.getLevel()), true)
							.addField("SSN", usr.getSSN().getSSNString(targetLevel == ownLevel), true)
							.addField("Discord name", m.getEffectiveName(), true)
							.addField("Discord id", usr.getDiscordUser().getId(), true)
							.addField("Voice Interface Location", m.getVoiceState().inVoiceChannel() ? m.getVoiceState().getChannel().getName() : "None", true)
							.addField("Online status", m.getOnlineStatus().getKey().replace("dnd", "Do not disturb"), true)
							.addField("Roles", m.getRoles().stream().map(Role::getName).reduce((s1, s2) -> s1 + '\n' + s2).orElse("None"), false)
							.addField("Aliases", usr.getAllAliases().stream().reduce((s1, s2) -> s1 + '\n' + s2).orElse("None"), false)
							.setColor(cls.getColor())
							.setThumbnail(usr.getAvatarWithClassUrl().get())
							.build());
						e.reactSuccess();
					} catch (IOException | InterruptedException e1) {
						e1.printStackTrace();
					}
				});
			}).setAliases("mon").setArguments("%user% | %ssn%").build(),
			command("chlvl", "Changes level of specified user", e -> {
				Matcher matcher = levelSet.matcher(e.getArgs());

				if (!matcher.matches() || matcher.groupCount() != 2) {
					e.reply(EmbedUtils.create(0xFF0000, "Invalid args: '" + e.getArgs() + "'", "Args should be '(user level)'", Classification.IRRELEVANT.getImg()));
					e.reactWarning();
					return;
				}
				int level = Integer.parseInt(matcher.group(2));
				int ownLevel = Users.of(e.getAuthor()).getLevel();
				if (level > ownLevel) {
					e.reply(EmbedUtils.create(0xFF0000, "You can't do this!", "You can't set level more than your own", Classification.RELEVANT_THREAT.getImg()));
					e.reactWarning();
					return;
				}
				TMBotUser usr = getUser(matcher.group(1), e);
				if (usr == null) {
					return;
				}
				if (usr.getLevel() >= ownLevel) {
					e.reply(EmbedUtils.create(0xFF0000, "No, no, no", "This user is to powerful, so you can't change his level.", Classification.RELEVANT_THREAT.getImg()));
					e.reactWarning();
					return;
				}
				usr.setLevel(level);
				reply(e, usr.getClassification().getColor(), "Success", "Level of " + usr.getName() + " changed to " + level, usr.getAvatarWithClassUrl());
				e.reactSuccess();

			}).setArguments("%user%:i%level%").setRequiredPermission(1).setOnDenied((p, e) -> {
				e.reply(EmbedUtils.create(0xFF0000, "Permission denied", "You really can't do anything with levels...", Classification.IRRELEVANT_THREAT.getImg()));
				e.reactWarning();
			}).build(),
			command("reclass", "Changes classification of specified user", e -> {
				Matcher matcher = classSet.matcher(e.getArgs());
				if (!matcher.matches() || matcher.groupCount() != 2) {
					e.reply(EmbedUtils.create(0xFF0000, "Invalid args: '" + e.getArgs() + "'", "Args should be '(user:class)'", Classification.IRRELEVANT.getImg()));
					e.reactWarning();
					return;
				}
				Classification classification = Classification.getClassification(matcher.group(2), true);
				if (classification == null) {
					e.reply(EmbedUtils.create(0xFF0000, "Classification not found", "Classification '" + matcher.group(2) + "' not found", Classification.UNKNOWN.getImg()));
					return;
				}
				TMBotUser usr = getUser(matcher.group(1), e);
				if (usr == null) {
					return;
				}
				if (usr.getLevel() > Users.of(e.getAuthor()).getLevel()) {
					e.reply(EmbedUtils.create(0xFF0000, "No, no, no", "This user is to powerful, so you can't change his classification", Classification.RELEVANT_THREAT.getImg()));
					e.reactWarning();
					return;
				}
				usr.setClassification(classification);
				reply(e, classification.getColor(), "Success", "Classification of " + usr.getName() + " changed to " + classification.getName(), usr.getAvatarWithClassUrl());
				e.reactSuccess();
			}).setArguments("%user%:%class%").setRequiredPermission(100).setOnDenied((p, e) -> {
				e.reply(EmbedUtils.create(0xFF0000, "Permission denied", "Only Admin or Primary Analog Interface can change classifications", Classification.RELEVANT_THREAT.getImg()));
				e.reactWarning();
			}).build(),
			command("alias", "Add or remove alias for user", e -> {
				e.reply(EmbedUtils.create(0xFF0000, "Invalid command", "User 'alias add' or 'alias remove'", Classification.IRRELEVANT.getImg()));
				e.reactWarning();
			}).setChildren(command("add", "Adds alias", e -> {
					Matcher m = classSet.matcher(e.getArgs());
					if (!m.matches() || m.groupCount() != 2) {
						e.reply(EmbedUtils.create(0xFF0000, "Invalid args: '" + e.getArgs() + "'", "Args should be '(user:alias)'", Classification.IRRELEVANT.getImg()));
						e.reactWarning();
						return;
					}
					String alias = m.group(2);
					TMBotUser usr = getUser(m.group(1), e);
					if (usr == null) {
						return;
					}
					usr.addAlias(alias);
					reply(e, usr.getClassification().getColor(), "Success", "Added alias '" + alias + "' to user '" + usr.getName() + "'.", usr.getAvatarWithClassUrl());
					e.reactSuccess();
				}).setArguments("%user%:%alias%").build(),
				command("remove", "Removes alias", e -> {
					TMBotUser usr = getUser(e.getArgs(), e);
					if (usr == null) {
						return;
					}
					usr.removeAlias(e.getArgs());
					reply(e, usr.getClassification().getColor(), "Success", "Removed alias '" + e.getArgs() + "' from user '" + usr.getName() + "'.", usr.getAvatarWithClassUrl());
					e.reactSuccess();
				}).setArguments("%alias%").build()).setRequiredPermission(100).setOnDenied((p, e) -> {
				e.reply(EmbedUtils.create(0xFF0000, "Permission denied", "Only Admin or Primary Analog Interface can change aliases", Classification.RELEVANT_THREAT.getImg()));
				e.reactWarning();
			}).setArguments("(add %user%:%alias%)|(remove %alias%)").build()
		};
	}

	private TMBotUser getUser(String name, CommandEvent e) {
		if (name.matches("\\d{3}-\\d{2}-\\d{4}")) {
			int ssn = 0;
			ssn += Integer.parseInt(name.substring(0, 3)) * 1000000;
			ssn += Integer.parseInt(name.substring(4, 6)) * 10000;
			ssn += Integer.parseInt(name.substring(7));
			return Users.of(ssn);
		}
		TMBotUser ret = Users.of(name);
		if (ret == null) {
			Wrapper<User> user = Getters.getUser(name);
			if (user.getState() == Wrapper.WrapperState.NONE) {
				e.reply(EmbedUtils.create(0xFF0000, "Cannot find user '" + name + "'", "Really can't...", Classification.UNKNOWN.getImg()));
				e.reactWarning();
				return null;
			}
			if (user.getState() == Wrapper.WrapperState.MULTI) {
				e.reply(EmbedUtils.create(0xFF0000, "Too many users!!!", "'Too many' is more than one", Classification.UNKNOWN.getImg()));
				e.reactWarning();
				return null;
			}
			//noinspection ConstantConditions (Checked by states)
			ret = Users.of(user.getSingle().get());
			if (ret == null) {
				e.reply(EmbedUtils.create(0xFF0000, "User not authenticated", "This user has not yet authenticated", Classification.UNKNOWN.getImg()));
				e.reactWarning();
				return null;
			}
		}
		return ret;
	}
}
