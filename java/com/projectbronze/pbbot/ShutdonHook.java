package com.projectbronze.pbbot;

import java.util.List;
import com.projectbronze.pbbot.data.AdvGuild;
import com.projectbronze.pbbot.data.AdvUser;
import com.projectbronze.pbbot.music.MusicHandler;
import net.dv8tion.jda.entities.Guild;

public class ShutdonHook
{
	private static int EXIT_CODE;
	private static Throwable ex;
	
	public static void exit(int code, Throwable cause)
	{
		EXIT_CODE = code;
		ex = cause;
		MusicHandler.shutdown();
		System.exit(code);
	}
	
	public static void init()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			AdvUser.saveUsers();
			AdvGuild.saveGuilds();
			switch (EXIT_CODE)
			{
				case (0):
					break;
				case (1):
				{
					if (ex == null)
					{
						break;
					}
					try
					{
						List<Guild> guilds = Core.bot.getGuilds();
						for (Guild g : guilds)
						{
							try
							{
								g.getTextChannels().get(0).sendMessage("Извините, я упал, возможно это может вам помочь: " + ex.getMessage() + ", если не помогло напишите автору.");
							}
							catch (Exception e)
							{
								if (Core.err != null && Core.err.canLog())
								{
									Core.err.error("Unable to send Core.error message to %s", g.getName());
								}
								else
								{
									System.err.printf("Unable to send Core.error message to %s\n", g.getName());
								}
							}
						}
						ex.printStackTrace(Core.err != null && Core.err.canLog() ? Core.err : System.err);
					}
					catch (Exception e)
					{
						ex.printStackTrace();
					}
				}
			}
		}));
	}
}
