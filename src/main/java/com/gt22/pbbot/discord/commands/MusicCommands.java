package com.gt22.pbbot.discord.commands;

import com.gt22.botrouter.api.misc.MiscUtils;
import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.music.MusicHandler;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.randomutils.utils.JoinUtils;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.entities.Guild;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.nio.file.Paths;

public class MusicCommands implements ICommandList {

	public static final AdvancedCategory cat = new AdvancedCategory("Music", new Color(0xAFEBF3), "http://52.48.142.75/ai.jpg");

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	@Override
	public Command[] init() {
		return new Command[]{
			command("play", "Play music", e -> {
				String args = e.getArgs();
				boolean all = false, noRepeat = true;
				int count = 1;
				String[] a = args.split(" ");
				List<String> resultArgs = new ArrayList<>();
				for (int i = 0; i < a.length; i++) {
					String arg = a[i];
					switch (arg) {
						case "-a":
						case "--all": {
							all = true;
							break;
						}
						case "-r":
						case "--allow-repeat": {
							noRepeat = false;
						}
						case "*": {
							try {
								i++;
								if (i >= a.length) {
									reply(e, 0xFF0000, "Count not specified", "Specify count after *");
									return;
								}
								count = Integer.parseInt(a[i]);
								break;
							} catch (NumberFormatException ex) {
								reply(e, 0xFF0000, "Invalid count", "Count should be a number", cat.getImg());
								return;
							}
						}
						default: {
							resultArgs.add(arg);
						}
					}
				}
				args = JoinUtils.join(resultArgs, " ").orElse("");
				MusicHandler.Return ret;
				System.out.println(noRepeat);
				if (args.isEmpty()) {
					ret = MusicHandler.addXTimes(MusicHandler.musicDir, count, noRepeat, all, e.getGuild());
				} else if (args.startsWith("http")) {
					ret = MusicHandler.addXTimes(new URL(args), count, noRepeat, e.getGuild());
				} else {
					ret = MusicHandler.addXTimes(MusicHandler.musicDir.resolve(args), count, noRepeat, all, e.getGuild());
				}
				switch (ret) {
					case OK: {
						reply(e, 0x00FF00, "Success", "", cat.getImg());
						e.reactSuccess();
						break;
					}
					case NOFILE: {
						reply(e, 0xFF0000, "Music not found", "", cat.getImg());
						e.reactWarning();
						break;
					}
					case CANNOT_ADD: {
						reply(e, 0xFF0000, "Something went wrong, contact author", "", cat.getImg());
						e.reactWarning();
						break;
					}
					case INVALID_FILE: {
						reply(e, 0xFF0000, "I can't play this", "", cat.getImg());
						e.reactWarning();
						break;
					}
				}
			}).setArguments("(%songName%|%songUrl%) [--all] [* i%count%]").build(),
			command("pause", "Pause playing", e -> {
				Guild g = e.getGuild();
				if(!MusicHandler.isPaused(g) && MusicHandler.playlistSize(g) > 0) {
					MusicHandler.pause(g);
					reply(e, 0x00FF00, "Paused", "", cat.getImg());
					e.reactSuccess();
				} else {
					reply(e, 0xFFFF00, "Not playing", "", cat.getImg());
					e.reactWarning();
				}
			}).build(),
			command("resume", "Resumes playing", e -> {
				if(MusicHandler.isPaused(e.getGuild())) {
					MusicHandler.resume(e.getGuild());
					reply(e, 0x00FF00, "Resumed", "", cat.getImg());
					e.reactSuccess();
				} else {
					reply(e, 0xFFFF00, "Not paused", "", cat.getImg());
					e.reactWarning();
				}
			}).build(),
			command("reset", "Clears playlist", e -> {
				if(MusicHandler.playlistSize(e.getGuild()) > 0) {
					MusicHandler.reset(e.getGuild());
					reply(e, 0x00FF00, "Reseted", "", cat.getImg());
				} else {
					reply(e, 0xFF0000, "Nothing playing", "", cat.getImg());
					e.reactWarning();
				}
			}).setAliases("clear").build(),
			command("skip", "Skips specified song", e -> {
				String args = e.getArgs();
				if(args.isEmpty()) {
					if(MusicHandler.playlistSize(e.getGuild()) > 0) {
						MusicHandler.skip(1, e.getGuild());
					} else {
						reply(e, 0xFF0000,"Playlist empty", "", cat.getImg());
						e.reactWarning();
						return;
					}
				} else {
					int n = Integer.parseInt(args);
					if(MusicHandler.playlistSize(e.getGuild()) >= n) {
						MusicHandler.skip(n, e.getGuild());
					} else {
						reply(e, 0xFF0000, "No such song in playlist", "", cat.getImg());
						e.reactWarning();
						return;
					}
				}
				reply(e, 0x00FF00, "Skipped", "", cat.getImg());
				e.reactSuccess();
			}).setArguments("[i%num%]").build(),
			command("playlist", "Shows playlist", e -> {
				Guild g = e.getGuild();
				StringBuilder playlistBuilder = new StringBuilder();
				int size = MusicHandler.playlistSize(g);
				if(size == 0) {
					e.reply("Playlist empty");
					e.reactWarning();
					return;
				}
				int playlistNum = 1;
				for(int i = 0; i < size; i++) {
					playlistBuilder.append(i + 1).append(": ").append(MusicHandler.playlistGet(i, g).getIdentifier().replace('\\', '/').substring("Music/".length())).append('\n');
					if(playlistBuilder.length() > 1800) {
						sendPlaylist(playlistBuilder.toString(), playlistNum++, e);
						playlistBuilder = new StringBuilder();
					}
				}
				if(playlistBuilder.length() != 0) {
					sendPlaylist(playlistBuilder.toString(), playlistNum == 1 ? -1 : playlistNum, e);
				}
				e.reactSuccess();
			}).build()
		};
	}

	private void sendPlaylist(String playlist, int part, CommandEvent e) {
		String title = part == -1 ? "Playlist" : "Playlist part " + part;
		e.reply(EmbedUtils.create(0x00FF00, title, playlist, cat.getImg()));
	}
}
