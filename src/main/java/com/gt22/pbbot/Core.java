package com.gt22.pbbot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gt22.pbbot.interfaces.ITMBModule;
import com.gt22.pbbot.utils.ConfigUtils;
import com.gt22.pbbot.utils.MiscUtils;
import com.gt22.randomutils.Instances;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Core {

	private static final SimpleLog LOG = SimpleLog.getLog("TMBot");
	private static Map<String, ITMBModule> MODULES = new HashMap<>();
	private static Config config;
	public static boolean hidingProtocol = true;
	public static void main(String[] args) throws Exception {
		for(String s : args) {
			if(s.equals("-d")) {
				SimpleLog.LEVEL = SimpleLog.Level.ALL;
			}
		}
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out"); //Redirect slf4j-simple to System.out from System.err
		config = ConfigUtils.loadConfig(Config.class, "config.json", new JsonObject());
		MiscUtils.ArrayUtils.forEach(config.MODULES, module -> {
			try {
				Class<?> clazz = Class.forName(module);
				if(ITMBModule.class.isAssignableFrom(clazz)) {
					Class<? extends ITMBModule> moduleClass = clazz.asSubclass(ITMBModule.class);
					try {
						ITMBModule m = moduleClass.newInstance();
						try {
							MODULES.put(m.name(), m);
							LOG.info(String.format("Loading module %s (%s)", m.name(), module));
							m.init();
						}catch (Exception e) {
							LOG.warn(String.format("Exception while initializing (init) module %s", module));
							LOG.log(e);
						}
					} catch (InstantiationException e) {
						LOG.warn(String.format("Exception while instantiating (newInstance) module %s", module));
						LOG.log(e);
					} catch (IllegalAccessException e) {
						LOG.warn(String.format("Unable to access default constructor of module %s", module));
					}
				} else {
					LOG.warn(String.format("Module %s is not subclass of 'ITMBModule' interface", module));
				}
			} catch (ClassNotFoundException e) {
				LOG.warn(String.format("Module %s not found", module));
			}
		});

		//Lock modules map after modules initialization is complete
		MODULES = Collections.unmodifiableMap(MODULES);
	}

	public static SimpleLog getCoreLog() {
		return LOG;
	}

	public static Config getConfig() {
		return config;
	}

	public static JsonObject getJsonReply(HttpUriRequest req) throws IOException {
		return Instances.getParser().parse(new InputStreamReader(Instances.getHttpClient().execute(req).getEntity().getContent())).getAsJsonObject();
	}

	public static JsonElement parse(Path file) throws IOException {
		BufferedReader r = Files.newBufferedReader(file);
		JsonElement ret = Instances.getParser().parse(r);
		r.close();
		return ret;
	}

	public static Map<String, ITMBModule> getModules() {
		return MODULES;
	}

	public static boolean isModuleLoaded(String name) {
		return MODULES.containsKey(name);
	}

	public static ITMBModule getModule(String name) {
		return MODULES.get(name);
	}
}
