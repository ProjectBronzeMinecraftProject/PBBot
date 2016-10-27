package com.projectbronze.pbbot.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.nohub.NohubConst;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class MiscUtils
{
	private static final URL pastebinURL;

	static
	{
		try
		{
			pastebinURL = new URL("http://pastebin.com/api/api_post.php");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace(Core.err);
			throw new RuntimeException();
		}
	}

	public static String getArrayAsString(Object[] array, String format)
	{
		if (array.length == 0)
		{
			return "";
		}
		String ret = "";
		for (Object o : array)
		{
			ret += o + format;
		}
		return ret.substring(0, ret.length() - format.length());
	}

	public static File getRandomFile(File md, Random r)
	{
		File music = md.listFiles()[r.nextInt(md.listFiles().length)];
		if (music.isDirectory())
		{
			return getRandomFile(music, r);
		}
		else
		{
			return music;
		}
	}

	public static String uploadToPastebin(String paste, String name)
	{
		byte[] post;
		try
		{
			post = ("api_dev_key=" + NohubConst.PASTEBINKEY + "&api_paste_code=" + URLEncoder.encode(paste, "UTF-8") + "&api_paste_private=" + 1 + "&api_paste_name=" + URLEncoder.encode(name, "UTF-8") + "&api_option=paste&api_paste_expire_date=10M").getBytes(StandardCharsets.UTF_8);

			HttpURLConnection conn = (HttpURLConnection) pastebinURL.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(post.length));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream()))
			{
				wr.write(post);
			}
			BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String response = "";
			while (response == "")
			{
				response = is.readLine();
			}
			is.close();
			return response;
		}
		catch (Exception e)
		{
			e.printStackTrace(Core.err);
			return "UNABLETOUPLOAD";
		}
	}

	public static boolean isInChannel(Guild guild)
	{
		return Core.bot.getAudioManager(guild).isConnected();
	}

	public static void forEachFileInDir(File dir, Consumer<File> action)
	{
		List<File> files = Arrays.asList(dir.listFiles());
		for (File f : files)
		{
			if (f.isDirectory())
			{
				forEachFileInDir(f, action);
			}
			else
			{
				action.accept(f);
			}
		}
	}

	public static String getTime(SimpleDateFormat format)
	{
		return format.format(Calendar.getInstance().getTime());
	}

	public static boolean isInBount(int val, int min, int max)
	{
		return min < val && val < max;
	}

	public static User getUser(String name, String discriminator)
	{
		return Core.bot.getUsersByName(name).stream().filter((user) -> user.getDiscriminator().equals(discriminator)).toArray(User[]::new)[0];
	}

	
	public static Wrapper<User> tryGetUser(String from)
	{
		User usr = Core.bot.getUserById(from);
		if (usr == null)
		{
			if (from.contains("#") && !from.startsWith("#"))
			{
				String[] str = from.split("#");
				usr = getUser(str[0], str[1]);
				if (usr != null)
				{
					return new Wrapper<User>(usr);
				}
			}
			else
			{
				if (from.startsWith("@"))
				{
					from = from.substring(1);
				}
				String f = from;
				if(f.startsWith("#"))
				{
					return new Wrapper<User>(Core.bot.getUsers().parallelStream().filter(user -> user.getDiscriminator().equals(f.substring(1))).toArray(User[]::new));
				}
				else
				{
					List<User> users = Core.bot.getUsersByName(from);
					return new Wrapper<User>(users.toArray(new User[users.size()]));
				}
			}
		}
		else
		{
			return new Wrapper<User>(usr);
		}
		return new Wrapper<User>();
	}
	
	public static Wrapper<TextChannel> tryGetChannel(String from, Guild guild)
	{
		TextChannel chan = Core.bot.getTextChannelById(from);
		if(chan == null)
		{
			String t = from.startsWith("#") ? from.substring(1) : from;
			List<TextChannel> chans = Core.bot.getTextChannelsByName(t);
			if(chans.size() == 1)
			{
				return new Wrapper<TextChannel>(chans.get(0));
			}
			else if(guild != null)
			{
				return new Wrapper<TextChannel>(guild.getTextChannels().parallelStream().filter(c -> c.getName().equals(t)).toArray(TextChannel[]::new));
			}
			else
			{
				return new Wrapper<TextChannel>(chans.toArray(new TextChannel[chans.size()]));
			}
		}
		else
		{
			return new Wrapper<TextChannel>(chan);
		}
		//return new Wrapper();
	}
}
