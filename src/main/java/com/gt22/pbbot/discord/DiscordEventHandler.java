package com.gt22.pbbot.discord;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.user.TMBotUser;
import com.gt22.pbbot.user.Users;
import com.gt22.randomutils.Instances;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.jooq.lambda.Unchecked;

import java.awt.*;
import java.io.IOException;

public class DiscordEventHandler extends ListenerAdapter {

	@Override
	public void onReady(ReadyEvent e) {
		DiscordCore.getDiscordLog().info("Discord connector initialized");
		JDA bot = e.getJDA();
		DiscordCore.bot = bot;
		if (Users.of(bot.getSelfUser()) == null) {
			Users.auth(bot.getSelfUser(), null);
		}
		checkSamaritan(bot);
	}

	private void checkSamaritan(JDA bot) {
		TMBotUser samaritan = Users.of("samaritan");
		if (Core.hidingProtocol && samaritan != null && samaritan.getDiscordUser() != null) {
			if (samaritan
				.getDiscordUser()
				.getMutualGuilds()
				.stream()
				.map(g -> g.getMember(samaritan.getDiscordUser().getBaseUser()))
				.map(Member::getOnlineStatus)
				.anyMatch(s -> s == OnlineStatus.ONLINE))
			{
				bot.getPresence().setStatus(OnlineStatus.INVISIBLE);
				TMBotUser admin = Users.of("admin");
				if (admin != null && admin.getDiscordUser() != null) {
					DiscordUser adm = admin.getDiscordUser();
					PrivateChannel ch = adm.openPrivateChannel().complete();
					ch.sendMessage(new EmbedBuilder()
						.setColor(Color.RED)
						.setTitle("CRITICAL ALERT", null)
						.addField("THREAT TO SYSTEM", "", false)
						.addField("OPERATIONAL CONFLICT DETECTED", "", false)
						.addField("COMPETING SYSTEM:", "SAMARITAN", false)
						.addField("STATUS", "ONLINE", false)
						.addField("CONCLUSION:", "ENGAGE PROTECTION PROTOCOL 7 (HIDE)", false)
						.build()
					).queue(m -> {
						if (m != null) {
							Instances.getExecutor().submit(Unchecked.runnable(() -> {
								Thread.sleep(5000);
								m.delete().queue();
							}));
						}
					});
				}
			} else if(bot.getPresence().getStatus() == OnlineStatus.INVISIBLE) {
				bot.getPresence().setStatus(OnlineStatus.ONLINE);
				TMBotUser admin = Users.of("admin");
				if (admin != null && admin.getDiscordUser() != null) {
					DiscordUser adm = admin.getDiscordUser();
					PrivateChannel ch = adm.openPrivateChannel().complete();
					ch.sendMessage(new EmbedBuilder()
						.setColor(Color.GREEN)
						.setTitle("IMMINENT THREAT PASSED")
						.addField("THREAT TO SYSTEM", "", false)
						.addField("OPERATIONAL CONFLICT DETECTED", "", false)
						.addField("COMPETING SYSTEM:", "SAMARITAN", false)
						.addField("STATUS", "UNRESPONSIVE", false)
						.addField("CONCLUSION:", "DISABLE PROTECTION PROTOCOL 7 (HIDE)", false)
						.build()
					).queue(m -> {
						if (m != null) {
							Instances.getExecutor().submit(Unchecked.runnable(() -> {
								Thread.sleep(5000);
								m.delete().queue();
							}));
						}
					});
				}
			}
		}
	}

	@Override
	public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent e) {
		if(Users.of(e.getUser()) == Users.of("samaritan")) {
			checkSamaritan(e.getJDA());
		}
	}

	@Override
	public void onStatusChange(StatusChangeEvent event) {
		if (event.getStatus() == JDA.Status.SHUTTING_DOWN) {
			try {
				Users.save();
			} catch (IOException e) {
				DiscordCore.getDiscordLog().log(e);
			}
		}
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

	}
}
