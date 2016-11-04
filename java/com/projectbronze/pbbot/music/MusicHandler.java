package com.projectbronze.pbbot.music;

import java.io.File;
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

public class MusicHandler
{
	private static List<Object> playlist = new ArrayList<Object>();
	private static Player currentPlayer;
	private static Guild guild;
	private static ExecutorService exe = Executors.newSingleThreadExecutor();
	public static final File musicDir = new File("Music/");

	public static enum Return
	{
		OK, NOFILE, NOURL;
	}

	public static Return add(String name, float volume, boolean file, Guild guild, boolean all, Random r)
	{
		if (file)
		{
			File music = new File(musicDir, name + (name.endsWith(".mp3") ? "" : ".mp3"));
			if (!music.exists())
			{
				File musicFolder = new File(musicDir, name);
				if (musicFolder.exists())
				{
					music = musicFolder;
				}
				else
				{
					return Return.NOFILE;
				}
			}
			if (music.isDirectory())
			{
				if (all)
				{
					MiscUtils.forEachFileInDir(music, (m) ->
					{
						playlist.add(m);
					});
					if (getState() == State.EMPTY)
					{
						setState(State.STOPPED);
						MusicHandler.guild = guild;
						exe.execute(playTask);

					}
					return Return.OK;
				}
				else
				{
					music = MiscUtils.getRandomFile(music, r);
				}
			}
			playlist.add(music);
		}
		else
		{
			URL music;
			try
			{
				music = new URL(name);
				URLPlayer p = new URLPlayer(Core.bot, music);
				p.play();
				playlist.add(music);
			}
			catch (Exception e)
			{
				return Return.NOURL;
			}
		}
		if (getState() == State.EMPTY)
		{
			setState(State.STOPPED);
			MusicHandler.guild = guild;
			exe.execute(playTask);
		}
		return Return.OK;
	}

	public static void pause()
	{
		lock.writeLock().lock();
		currentPlayer.pause();
		lock.writeLock().unlock();
		setState(State.PAUSE);
	}

	public static void resume()
	{
		lock.writeLock().lock();
		currentPlayer.play();
		lock.writeLock().unlock();
		setState(State.PLAYING);
	}

	public static void skip(int i)
	{
		lock.writeLock().lock();
		if (--i <= 0)
		{
			if(currentPlayer != null)
			{
				currentPlayer.stop();
			}
			else
			{
				playlist.remove(0);
			}
		}
		else
		{
			playlist.remove(i);
		}
		lock.writeLock().unlock();
	}

	public static void reset()
	{
		lock.writeLock().lock();
		if (currentPlayer != null)
		{
			currentPlayer.stop();
			currentPlayer = null;
		}
		playlist.clear();
		lock.writeLock().unlock();
		setState(State.EMPTY);
	}

	public static enum State
	{
		EMPTY(0), PLAYING(1), STOPPED(2), PAUSE(3);
		public final int idx;

		private State(int idx)
		{
			this.idx = idx;
		}

		public static State fromIdx(int idx)
		{
			for (State s : State.values())
			{
				if (s.idx == idx)
				{
					return s;
				}
			}
			return null;
		}
	}

	public static void setState(State s)
	{
		lock.writeLock().lock();
		state.set(s.idx);
		lock.writeLock().unlock();
	}

	public static State getState()
	{
		return State.fromIdx(state.get());
	}

	public static void playlistAdd(Object o)
	{
		lock.writeLock().lock();
		playlist.add(o);
		lock.writeLock().unlock();
	}

	public static Object playlistGet(int i)
	{
		Object ret;
		lock.readLock().lock();
		ret = playlist.get(i);
		lock.readLock().unlock();
		return ret;
	}

	public static int playlistSize()
	{
		int i;
		lock.readLock().lock();
		i = playlist.size();
		lock.readLock().unlock();
		return i;
	}

	public static void playlistRemove(int i)
	{
		lock.writeLock().lock();
		playlist.remove(i);
		lock.writeLock().unlock();
	}

	public static void setPlayer(Player p)
	{
		lock.writeLock().lock();
		currentPlayer = p;
		lock.writeLock().unlock();
	}

	private static AtomicInteger state = new AtomicInteger();
	private static ReadWriteLock lock = new ReentrantReadWriteLock();
	private static Runnable playTask = () ->
	{
		loop:
		while (true)
		{
			switch (getState())
			{
				case STOPPED:
				{
					if (checkForEmpty())
					{
						break;
					}
					Player p = null;
					try
					{
						Object o = playlistGet(0);
						if (o instanceof File)
						{
							p = new FilePlayer((File) o);
							String file = ((File) o).getName();
							Core.bot.getAccountManager().setGame(file.substring(0, file.lastIndexOf('.')));
						}
						else if (o instanceof URL)
						{
							p = new URLPlayer(guild.getJDA(), (URL) o);
							String file = ((URL) o).getFile();
							Core.bot.getAccountManager().setGame(file.substring(0, file.lastIndexOf('.')));
						}
						else
						{
							skip(1);
						}
					}
					catch (Exception e)
					{
						skip(1);
						e.printStackTrace();
						break;
					}
					if (p == null)
					{
						skip(1);
						break;
					}
					p.setVolume(0.025F);
					try
					{
						guild.getAudioManager().setSendingHandler(p);
						p.play();
						setPlayer(p);
						setState(State.PLAYING);
					}
					catch (Exception e)
					{
						skip(1);
						e.printStackTrace();
					}
					break;
				}
				case PLAYING:
				{
					lock.readLock().lock();
					boolean stopped = currentPlayer.isStopped();
					lock.readLock().unlock();
					if (checkForEmpty())
					{
						break;
					}
					if (stopped)
					{
						playlistRemove(0);
						setState(State.STOPPED);
					}
					break;
				}
				case PAUSE:
				{
					if (checkForEmpty())
					{
						break;
					}
					lock.readLock().lock();
					boolean paused = currentPlayer.isPaused();
					lock.readLock().unlock();
					if (!paused)
					{
						lock.writeLock().lock();
						currentPlayer.pause();
						lock.writeLock().unlock();
					}
					break;
				}
				case EMPTY:
				{
					Core.bot.getAccountManager().setGame("!команды");
					break loop;
				}
			}
		}
	};

	private static boolean checkForEmpty()
	{
		lock.readLock().lock();
		boolean empty = playlist.isEmpty();
		lock.readLock().unlock();
		if (empty)
		{
			setState(State.EMPTY);
		}
		return empty;
	}

	public static void shutdown()
	{
		exe.shutdownNow();
	}
}