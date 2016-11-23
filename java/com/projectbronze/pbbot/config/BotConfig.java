package com.projectbronze.pbbot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.projectbronze.pbbot.Core;

public class BotConfig {
	private static final File CONFIG;
	public static final String[] DEFAULT_ADMINS;
	public static final String TOKEN, PASTEBIN_KEY, ADD_QUOTE_KEY, ADD_QUOTE_URL, GET_QUOTE_URL;
	static {
		try {
			String jarDir = BotConfig.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", "\\ ");
			if(jarDir.endsWith(".jar"))
			{
				jarDir = jarDir.substring(0, jarDir.lastIndexOf('/'));
			}
			CONFIG = new File(jarDir, "config.json");
			Gson gson = Core.gson;
			JsonObject cfg = new JsonParser().parse(new InputStreamReader(new FileInputStream(CONFIG))).getAsJsonObject();
			DEFAULT_ADMINS = gson.fromJson(cfg.getAsJsonArray("defaultAdmins"), String[].class);
			TOKEN = cfg.get("token").getAsString();
			PASTEBIN_KEY = cfg.get("pastebinKey").getAsString();
			JsonObject quotes = cfg.getAsJsonObject("quotes");
			ADD_QUOTE_KEY = quotes.get("addQuoteKey").getAsString();
			ADD_QUOTE_URL = quotes.get("addQuoteURL").getAsString();
			GET_QUOTE_URL = quotes.get("getQuoteURL").getAsString();
		} catch (JsonSyntaxException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
