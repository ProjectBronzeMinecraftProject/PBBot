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
import java.util.Random;
import java.util.function.Consumer;

import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.config.BotConfig;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GameImpl;


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
	
	public static void setTrackAsGame(AudioTrack track) {
			String name = track.getIdentifier();
			setGame(name.substring(name.lastIndexOf(File.separator) + 1, name.lastIndexOf('.')));
	}
	
	public static void setGame(String game) {
			System.out.println(game);
			Core.bot.getPresence().setGame(new GameImpl(game, null, Game.GameType.DEFAULT));
	}
	
}
