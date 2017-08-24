package com.gt22.pbbot;

import com.google.gson.JsonObject;
import com.gt22.botrouter.api.misc.RsaKeyLoaders;
import com.gt22.pbbot.utils.ConfigUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class Config {
	public String TOKEN;
	public String PASTEBIN_KEY;
	public String ROUTER_IP;
	public String IMAGE_DB_MANAGER;
	public String ROUTER_ID;
	public String OWNER;
	public String[] CO_OWNERS;
	public String QUOTER_URL;
	public String QUOTER_KEY;
	public String[] MODULES;
	public String PRIVATE_KEY;
	@ConfigUtils.ManualConfigProperty
	public Map<String, Pair<String, PublicKey>> PUBLIC_KEYS;

	public void loadManual(JsonObject cfg) {
		PUBLIC_KEYS = new HashMap<>();
		JsonObject keys = cfg.getAsJsonObject("PUBLIC_KEYS");
		keys.entrySet().forEach(Unchecked.consumer(e -> {
			JsonObject key = e.getValue().getAsJsonObject();
			PUBLIC_KEYS.put(e.getKey(), Pair.of(key.get("name").getAsString(), RsaKeyLoaders.loadPublic(key.get("key").getAsString())));
		}));
	}
}
