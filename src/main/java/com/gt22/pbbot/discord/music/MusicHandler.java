package com.gt22.pbbot.discord.music;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.utils.MiscUtils;
import com.gt22.randomutils.Instances;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class MusicHandler {
  private static final HashMap<String, GuildMusicManager> MANAGERS = new HashMap<>();
  public static Path musicDir;

  public enum Return {
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
    m.player.setVolume(200);
    return m;
  }

  public static Return addXTimes(URL file, int times, boolean noRepeat, Guild guild) {
    int j = 0;
    for (int i = 0; i < times; i++) {
      if (add(file, guild, noRepeat) == Return.CANNOT_ADD) {
        if (j++ > 1000) {
          return Return.CANNOT_ADD;
        }
        i--;
      }
    }
    return Return.OK;
  }

  public static Return addXTimes(Path file, int times, boolean noRepeat, boolean all, Guild guild) throws IOException {
    int j = 0;
    for (int i = 0; i < times; i++) {
      if (add(file, guild, noRepeat, all) == Return.CANNOT_ADD) {
        if (j++ > 1000) {
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
			  //noinspection StatementWithEmptyBody
			  while (!sh.addIfNotContains(t.get(Instances.getRand().nextInt(t.size()))) || i++ > 1000);
          } else {
            sh.queue(t.get(Instances.getRand().nextInt(t.size())));
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

  public static Return add(Path file, Guild guild, boolean noRepeat, boolean all) throws IOException {
    GuildMusicManager man = getManager(guild);
    TrackScheduler sh = man.scheduler;
    if (Files.isDirectory(file)) {
      file = MiscUtils.getRandomFile(file);
    }
    if(file == null) {
    	return Return.NOFILE;
	}
    try {
      PLAYER_MANAGER.loadItemOrdered(man, file.toString(), new AudioLoadResultHandler() {

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
				//noinspection StatementWithEmptyBody
				while (!sh.addIfNotContains(t.get(Instances.getRand().nextInt(t.size()))) || i++ > 1000);
            } else {
              sh.queue(t.get(Instances.getRand().nextInt(t.size())));
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
