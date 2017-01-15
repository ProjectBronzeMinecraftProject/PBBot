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
import com.projectbronze.pbbot.config.BotConfig;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class MiscUtils {
	private static final URL pastebinURL;

	static {
		try {
			pastebinURL = new URL("http://pastebin.com/api/api_post.php");
		} catch (MalformedURLException e) {
			e.printStackTrace(Core.err);
			throw new RuntimeException();
		}
	}

	public static String getArrayAsString(Object[] array, String format) {
		return Arrays.stream(array).map(Object::toString).reduce((s1, s2) -> s1 + format + s2).orElse("");
	}

	public static File getRandomFile(File dir, Random r) {
		if (dir.isFile()) {
			return dir;
		}
		File[] files = dir.listFiles();
		if (files == null) {
			return null;
		}
		File file = files[r.nextInt(files.length)];
		if (file.isDirectory()) {
			return getRandomFile(file, r);
		} else {
			return file;
		}
	}

	public static String uploadToPastebin(String paste, String name) {
		try {
			byte[] post = ("api_dev_key=" + BotConfig.PASTEBIN_KEY + "&api_paste_code=" + URLEncoder.encode(paste, "UTF-8") + "&api_paste_private=" + 1 + "&api_paste_name=" + URLEncoder.encode(name, "UTF-8") + "&api_option=paste&api_paste_expire_date=10M").getBytes(StandardCharsets.UTF_8);
			HttpURLConnection conn = (HttpURLConnection) pastebinURL.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(post.length));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(post);
			}
			BufferedReader is = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String response = "";
			while (response == "") {
				response = is.readLine();
			}
			is.close();
			return response;
		} catch (Exception e) {
			e.printStackTrace(Core.err);
			return "UNABLETOUPLOAD";
		}
	}

	public static boolean isInChannel(Guild guild) {
		return guild.getAudioManager().isConnected();
	}

	public static void forEachFileInDir(File dir, Consumer<File> action) {
		if(dir.isFile()) {
			action.accept(dir);
		} else {
			File[] files = dir.listFiles();
			for(File f : dir.listFiles()) {
				forEachFileInDir(f, action);
			}
		}
	}

	public static String getTime(SimpleDateFormat format) {
		return format.format(Calendar.getInstance().getTime());
	}

	public static boolean isInBount(int val, int min, int max) {
		return min < val && val < max;
	}

	public static User getUser(String name, String discriminator) {
		return Core.bot.getUsersByName(name).parallelStream().filter(user -> user.getDiscriminator().equals(discriminator)).findFirst().orElse(null);
	}

	public static Wrapper<User> tryGetUser(String from) {
		User usr = Core.bot.getUserById(from);
		if (usr == null) {
			if (from.contains("#") && !from.startsWith("#")) {
				String[] str = from.split("#");
				return new Wrapper<User>(getUser(str[0], str[1]));
			} else {
				String f = from.startsWith("@") ? from.substring(1) : from;
				if (f.startsWith("#")) {
					return new Wrapper<User>(Core.bot.getUsers().parallelStream().filter(user -> user.getDiscriminator().equals(f.substring(1))).toArray(User[]::new));
				} else {
					List<User> users = Core.bot.getUsersByName(from);
					return new Wrapper<User>(users.toArray(new User[users.size()]));
				}
			}
		}
		return new Wrapper<User>(usr);
	}

	public static Wrapper<TextChannel> tryGetChannel(String from, Guild guild) {
		TextChannel chan = Core.bot.getTextChannelById(from);
		if (chan == null) {
			String t = from.startsWith("#") ? from.substring(1) : from;
			if (guild != null) {
				return new Wrapper<TextChannel>(guild.getTextChannels().parallelStream().filter(c -> c.getName().equals(t)).toArray(TextChannel[]::new));
			} else {
				List<TextChannel> chans = Core.bot.getTextChannelsByName(t);
				if (chans.size() == 1) {
					return new Wrapper<TextChannel>(chans.get(0));
				} else {
					return new Wrapper<TextChannel>(chans.toArray(new TextChannel[chans.size()]));
				}
			}
		}
		return new Wrapper<TextChannel>(chan);
	}
}
