package com.projectbronze.pbbot.music;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.MiscUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;


public class MusicHandler {
	private static final HashMap<String, GuildMusicManager> MANAGERS = new HashMap<>();
	public static File musicDir;

	public static enum Return {
		OK,
		NOFILE,
		NOTFILE,
		INVALID_FILE,
		CANNOT_ADD;
	}

	private static final AudioPlayerManager PLAYER_MANAGER;
	static {
		PLAYER_MANAGER = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
		AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
	}

	public static GuildMusicManager getManager(Guild g) {
		String id = g.getId();
		GuildMusicManager m = MANAGERS.get(g.getId());
		if (m == null) {
			MANAGERS.put(id, m = new GuildMusicManager(PLAYER_MANAGER));
		}
		g.getAudioManager().setSendingHandler(m.getSendHandler());
		m.player.setVolume(10);
		return m;
	}

	public static Return addXTimes(URL file, int times, boolean noRepeat, Guild guild) {
		int j = 0;
		for (int i = 0; i < times; i++) {
			if (add(file, guild, noRepeat) == Return.CANNOT_ADD) {
				if (j > 1000) {
					return Return.CANNOT_ADD;
				}
				i--;
			}
		}
		return Return.OK;
	}

	public static Return addXTimes(File file, int times, boolean noRepeat, boolean all, Guild guild) {
		int j = 0;
		for (int i = 0; i < times; i++) {
			if (add(file, guild, noRepeat, all) == Return.CANNOT_ADD) {
				if (j > 1000) {
					return Return.CANNOT_ADD;
				}
				i--;

			}
		}
		return Return.OK;
	}

	public static Return add(URL url, Guild guild, boolean noRepeat) {
		GuildMusicManager man = getManager(guild);
		TrackScheduler sh = man.scheduler;
		try {
			PLAYER_MANAGER.loadItemOrdered(man, url.toExternalForm(), new AudioLoadResultHandler() {

				@Override
				public void trackLoaded(AudioTrack track) {
					if (noRepeat) {
						sh.addIfNotContains(track);
						throw new IllegalArgumentException("CANNOTADD");
					} else {
						sh.queue(track);
					}
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					List<AudioTrack> t = playlist.getTracks();
					if (noRepeat) {
						int i = 0;
						while (!sh.addIfNotContains(t.get(Core.rand.nextInt(t.size()))) || i++ > 1000);
					} else {
						sh.queue(t.get(Core.rand.nextInt(t.size())));
					}
				}

				@Override
				public void noMatches() {
					throw new IllegalArgumentException("NOMATCH");

				}

				@Override
				public void loadFailed(FriendlyException exception) {
					throw new IllegalArgumentException("LOAD:::" + exception.getLocalizedMessage());
				}
			});
		} catch (IllegalArgumentException e) {
			if (e.getMessage().startsWith("NOMATCH")) {
				return Return.NOFILE;
			}
			if (e.getMessage().startsWith("LOAD:::")) {
				return Return.INVALID_FILE;
			}
			if (e.getMessage().startsWith("CANNOTADD")) {
				return Return.CANNOT_ADD;
			}
		}
		return Return.OK;
	}

	public static Return add(File file, Guild guild, boolean noRepeat, boolean all) {
		GuildMusicManager man = getManager(guild);
		TrackScheduler sh = man.scheduler;
		if (file.isDirectory()) {
			file = MiscUtils.getRandomFile(file, Core.rand);
		}
		try {
			PLAYER_MANAGER.loadItemOrdered(man, file.getPath(), new AudioLoadResultHandler() {

				@Override
				public void trackLoaded(AudioTrack track) {
					if (noRepeat) {
						sh.addIfNotContains(track);
						throw new IllegalArgumentException("CANNOTADD");
					} else {
						sh.queue(track);
					}
				}

				@Override
				public void playlistLoaded(AudioPlaylist playlist) {
					List<AudioTrack> t = playlist.getTracks();
					if (all) {
						t.forEach(track -> {
							if (noRepeat) {
								sh.addIfNotContains(track);
							} else {
								sh.queue(track);
							}
						});
					} else {
						if (noRepeat) {
							int i = 0;
							while (!sh.addIfNotContains(t.get(Core.rand.nextInt(t.size()))) || i++ > 1000);
						} else {
							sh.queue(t.get(Core.rand.nextInt(t.size())));
						}

					}
				}

				@Override
				public void noMatches() {
					throw new IllegalArgumentException("NOMATCH");

				}

				@Override
				public void loadFailed(FriendlyException exception) {
					throw new IllegalArgumentException("LOAD:::" + exception.getLocalizedMessage());
				}
			});
		} catch (IllegalArgumentException e) {
			if (e.getMessage().startsWith("NOMATCH")) {
				return Return.NOFILE;
			}
			if (e.getMessage().startsWith("LOAD:::")) {
				return Return.INVALID_FILE;
			}
			if (e.getMessage().startsWith("CANNOTADD")) {
				return Return.CANNOT_ADD;
			}
		}
		return Return.OK;
	}

	public static void pause(Guild g) {
		getManager(g).player.setPaused(true);
	}

	public static void resume(Guild g) {
		getManager(g).player.setPaused(false);
	}

	public static void skip(int i, Guild g) {
		GuildMusicManager m = getManager(g);
		if (--i <= 0) {
			m.scheduler.nextTrack();
		} else {
			m.scheduler.skipTack(i);
		}
	}

	public static void reset(Guild g) {
		GuildMusicManager m = getManager(g);
		m.player.stopTrack();
		m.scheduler.clear();
	}

	public static AudioTrack playlistGet(int i, Guild g) {
		GuildMusicManager man = getManager(g);
		if (i <= 0) {
			return man.player.getPlayingTrack();
		}
		return man.scheduler.get(i - 1);
	}

	public static int playlistSize(Guild g) {
		GuildMusicManager man = getManager(g);
		return man.scheduler.queue.size() + (man.player.getPlayingTrack() == null ? 0 : 1);
	}
}