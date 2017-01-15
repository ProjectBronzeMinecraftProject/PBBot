package com.projectbronze.pbbot.music;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.MiscUtils;

import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.audio.player.URLPlayer;
import net.dv8tion.jda.entities.Guild;

public class MusicHandler {
	private static List<Player> playlist = new ArrayList<Player>();
	private static Player currentPlayer;
	public static Guild guild;
	private static ExecutorService exe = Executors.newSingleThreadExecutor();
	public static final File musicDir = new File("Music/");
	private static final Field URL_IN_PLAYER, FILE_IN_PLAYER;
	static {
		try {
			URL_IN_PLAYER = URLPlayer.class.getDeclaredField("urlOfResource");
			FILE_IN_PLAYER = FilePlayer.class.getDeclaredField("audioFile");
			URL_IN_PLAYER.setAccessible(true);
			FILE_IN_PLAYER.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public static enum Return {
		OK,
		NOFILE,
		NOTFILE,
		INVALID_FILE,
		CANNOT_ADD;
	}

	public static Return add(String name, float volume, boolean file, Guild guild, boolean all, Random r) {
		/*
		 * if (file) { File music = new File(musicDir, name +
		 * (name.endsWith(".mp3") ? "" : ".mp3")); if (!music.exists()) { File
		 * musicFolder = new File(musicDir, name); if (musicFolder.exists()) {
		 * music = musicFolder; } else { return Return.NOFILE; } } if
		 * (music.isDirectory()) { if (all) { MiscUtils.forEachFileInDir(music,
		 * playlist::add); if (getState() == State.EMPTY) {
		 * setState(State.STOPPED); MusicHandler.guild = guild;
		 * exe.execute(playTask);
		 * 
		 * } return Return.OK; } else { music = MiscUtils.getRandomFile(music,
		 * r); } } playlist.add(music); } else { URL music; try { music = new
		 * URL(name); URLPlayer p = new URLPlayer(Core.bot, music); //p.play();
		 * playlist.add(music); } catch (Exception e) { return Return.NOURL; } }
		 * if (getState() == State.EMPTY) { setState(State.STOPPED);
		 * MusicHandler.guild = guild; exe.execute(playTask); }
		 */
		return Return.OK;
	}

	public static Return addSingle(File file) {
		if (!file.exists()) {
			return Return.NOFILE;
		}
		if (!file.isFile()) {
			return Return.NOTFILE;
		}
		try {
			playlistAdd(new FilePlayer(file));
		} catch (Exception e) {
			e.printStackTrace(Core.err);
			return Return.INVALID_FILE;
		}
		return Return.OK;
	}

	public static Return addRandom(File dir, Random r) {
		if (dir.isFile()) {
			return addSingle(dir);
		}
		return addSingle(MiscUtils.getRandomFile(dir, r));
	}

	public static Return addXTimes(File fileOrDir, Random r, int times, boolean noRepeat) {
		int iter = 0;
		for (int i = 0; i < times; i++) {
			try {
				File f = MiscUtils.getRandomFile(fileOrDir, r);
				if (f == null) {
					if(!(f = new File(fileOrDir.getAbsolutePath() + ".mp3")).exists())
					{
						return Return.INVALID_FILE;
					}
				}
				if (noRepeat) {
					lock.readLock().lock();
					if (playlist.parallelStream().filter(p -> p instanceof FilePlayer).map(p -> getFileFromPlayer((FilePlayer) p)).anyMatch(f::equals)) {
						i--;
						iter++;
						lock.readLock().unlock();
						if (iter > 10000) {
							return Return.CANNOT_ADD;
						}
						continue;
					}
					lock.readLock().unlock();
				}
				playlistAdd(new FilePlayer(f));
			} catch (Exception e) {
				e.printStackTrace(Core.err);
				return Return.INVALID_FILE;
			}

		}
		return Return.OK;
	}

	public static Return addAll(File from) {
		if(!from.exists()) {
			File f = new File(from.getAbsolutePath() + ".mp3");
			if(f.exists()) {
				MusicHandler.addSingle(f);
			} else {
				return Return.NOFILE;
			}
		}
		MiscUtils.forEachFileInDir(from, MusicHandler::addSingle);
		return Return.OK;
	}

	public static Return addSingle(URL url) {

		try {
			playlistAdd(new URLPlayer(Core.bot, url));
		} catch (Exception e) {
			e.printStackTrace(Core.err);
			return Return.INVALID_FILE;
		}
		return Return.OK;
	}

	public static Return addXTimes(URL url, Random r, int times, boolean noRepeat) {
		int iter = 0;
		for (int i = 0; i < times; i++) {
			try {
				if (noRepeat) {
					lock.readLock().lock();
					if (playlist.parallelStream().filter(p -> p instanceof URLPlayer).map(p -> getURLFromPlayer((URLPlayer) p)).anyMatch(url::equals)) {
						i--;
						iter++;
						lock.readLock().unlock();
						if (iter > 100) {
							return Return.CANNOT_ADD;
						}
						continue;
					}
					lock.readLock().unlock();
				}
				iter = 0;
				playlistAdd(new URLPlayer(Core.bot, url));
			} catch (Exception e) {
				e.printStackTrace(Core.err);
				return Return.INVALID_FILE;
			}

		}
		return Return.OK;
	}

	public static void pause() {
		lock.writeLock().lock();
		currentPlayer.pause();
		lock.writeLock().unlock();
		setState(State.PAUSE);
	}

	public static void resume() {
		lock.writeLock().lock();
		currentPlayer.play();
		lock.writeLock().unlock();
		setState(State.PLAYING);
	}

	public static void skip(int i) {
		lock.writeLock().lock();
		if (--i <= 0) {
			if (currentPlayer != null) {
				currentPlayer.stop();
			} else {
				playlist.remove(0);
			}
		} else {
			playlist.remove(i);
		}
		lock.writeLock().unlock();
	}

	public static void reset() {
		lock.writeLock().lock();
		if (currentPlayer != null) {
			currentPlayer.stop();
			currentPlayer = null;
		}
		playlist.clear();
		lock.writeLock().unlock();
		setState(State.EMPTY);
	}

	public static enum State {
		EMPTY(0),
		PLAYING(1),
		STOPPED(2),
		PAUSE(3);
		public final int idx;

		private State(int idx) {
			this.idx = idx;
		}

		public static State fromIdx(int idx) {
			for (State s : State.values()) {
				if (s.idx == idx) {
					return s;
				}
			}
			return null;
		}
	}

	public static void setState(State s) {
		lock.writeLock().lock();
		state.set(s.idx);
		lock.writeLock().unlock();
	}

	public static State getState() {
		return State.fromIdx(state.get());
	}

	public static void playlistAdd(Player p) {
		lock.writeLock().lock();
		playlist.add(p);
		lock.writeLock().unlock();
		if (getState() == State.EMPTY) {
			setState(State.STOPPED);
			exe.execute(playTask);
		}
	}

	public static Player playlistGet(int i) {
		Player ret;
		lock.readLock().lock();
		ret = playlist.get(i);
		lock.readLock().unlock();
		return ret;
	}

	public static int playlistSize() {
		int i;
		lock.readLock().lock();
		i = playlist.size();
		lock.readLock().unlock();
		return i;
	}

	public static void playlistRemove(int i) {
		lock.writeLock().lock();
		playlist.remove(i);
		lock.writeLock().unlock();
	}

	public static void setPlayer(Player p) {
		lock.writeLock().lock();
		currentPlayer = p;
		lock.writeLock().unlock();
	}
	
	public static Player getPlayer() {
		Player ret;
		lock.readLock().lock();
		ret  = currentPlayer;
		lock.readLock().unlock();
		return ret;
	}

	private static AtomicInteger state = new AtomicInteger();
	private static ReadWriteLock lock = new ReentrantReadWriteLock();
	private static Runnable playTask = () -> {
		loop: while (true) {
			switch (getState()) {
				case STOPPED: {
					if (checkForEmpty()) {
						break;
					}
					Player p = playlistGet(0);
					try {
						String file = "!комманды.";
						if (p instanceof FilePlayer) {
							file = getFileFromPlayer((FilePlayer) p).getName();

						} else if (p instanceof URLPlayer) {
							file = getURLFromPlayer((URLPlayer) p).getFile();
						} else {
							skip(1);
						}
						Core.bot.getAccountManager().setGame(file.substring(0, file.lastIndexOf('.')));
					} catch (Exception e) {
						skip(1);
						e.printStackTrace(Core.err);
						break;
					}

					if (p == null) {
						skip(1);
						break;
					}
					p.setVolume(0.025F);
					try {
						guild.getAudioManager().setSendingHandler(p);
						p.play();
						setPlayer(p);
						setState(State.PLAYING);
					} catch (Exception e) {
						skip(1);
						e.printStackTrace(Core.err);
					}
					break;
				}
				case PLAYING: {
					lock.readLock().lock();
					boolean stopped = currentPlayer == null ? false : currentPlayer.isStopped();
					lock.readLock().unlock();
					if (checkForEmpty()) {
						break;
					}
					if (stopped) {
						playlistRemove(0);
						setState(State.STOPPED);
					}
					break;
				}
				case PAUSE: {
					if (checkForEmpty()) {
						break;
					}
					lock.readLock().lock();
					boolean paused = currentPlayer.isPaused();
					lock.readLock().unlock();
					if (!paused) {
						lock.writeLock().lock();
						currentPlayer.pause();
						lock.writeLock().unlock();
					}
					break;
				}
				case EMPTY: {
					Core.bot.getAccountManager().setGame("!команды");
					break loop;
				}
			}
		}
	};

	private static boolean checkForEmpty() {
		lock.readLock().lock();
		boolean empty = playlist.isEmpty();
		lock.readLock().unlock();
		if (empty) {
			setState(State.EMPTY);
		}
		return empty;
	}

	public static void shutdown() {
		exe.shutdownNow();
	}

	public static File getFileFromPlayer(FilePlayer p) {
		try {
			return (File) FILE_IN_PLAYER.get(p);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static URL getURLFromPlayer(URLPlayer p) {
		try {
			return (URL) URL_IN_PLAYER.get(p);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}