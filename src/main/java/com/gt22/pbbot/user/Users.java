package com.gt22.pbbot.user;

import com.google.gson.JsonObject;
import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.DiscordUser;
import com.gt22.randomutils.Instances;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.jooq.lambda.Unchecked;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Users {
	private static final List<JsonObject> USERS_INFO = new ArrayList<>();
	private static final Map<String, TMBotUser> USERS_BY_NAME = new HashMap<>();
	private static final Map<String, TMBotUser> USERS_BY_ALIAS = new HashMap<>();
	private static final Map<Integer, TMBotUser> USERS_BY_SSN = new HashMap<>();
	private static final Map<User, TMBotUser> USERS_BY_DISCORD = new HashMap<>();
	private static final Path USERS_DIR = Paths.get("internal", "users");
	private static final SimpleLog log = SimpleLog.getLog("PBBot#Users");


	static {
		Path users = Paths.get("users.json");
		if (Files.exists(users)) {
			log.info("Loading users.json");
			try {
				Core.parse(users).getAsJsonArray().forEach(e -> USERS_INFO.add(e.getAsJsonObject()));
			} catch (IOException e) {
				log.fatal("Unable to load users.json");
				log.log(e);
			}
		} else {
			log.info("users.json not found, no reserved users loaded");
		}
		try {
			Files.list(USERS_DIR).forEach(Unchecked.consumer(p -> {
				String name = p.getFileName().toString();
				name = name.substring(0, name.indexOf(".json"));
				mapUser(new TMBotUser(name));
			}));
		} catch (IOException e) {
			log.fatal("Unable to load users");
			log.log(e);
		}
	}

	public static Optional<JsonObject> getReservedUser(User discordUser) {
		return USERS_INFO.parallelStream()
			.filter(u -> u.has("discord"))
			.filter(u -> u.get("discord").getAsString().equals(discordUser.getId()))
			.findAny();
	}

	public static Optional<JsonObject> getReservedUser(String name) {
		return USERS_INFO.parallelStream()
			.filter(u -> u.get("name").getAsString().equals(name))
			.findAny();
	}

	public static TMBotUser of(User discordUser) {
		return USERS_BY_DISCORD.get(discordUser);
	}

	public static TMBotUser of(String name) {
		name = name.toLowerCase();
		TMBotUser ret = USERS_BY_NAME.get(name);
		if(ret == null) {
			return USERS_BY_ALIAS.get(name);
		} else {
			return ret;
		}
	}

	public static TMBotUser of(int ssn) {
		return USERS_BY_SSN.get(ssn);
	}

	public enum AuthState {
		SUCCESS,
		NAME_ALREADY_AUTHENTICATED,
		USER_ALREADY_AUTHENTICATED,
		RESERVED_NAME
	}

	public static AuthState auth(User discordUser, String name) {
		if(USERS_BY_DISCORD.containsKey(discordUser)) {
			return AuthState.USER_ALREADY_AUTHENTICATED;
		}
		Optional<JsonObject> reservedUser = getReservedUser(discordUser);
		if(reservedUser.isPresent()) {
			name = reservedUser.get().get("name").getAsString();
		} else {
			if(name == null) {
				name = discordUser.getName();
			}
			Optional<JsonObject> reservedName = getReservedUser(name);
			if(reservedName.isPresent()) {
				JsonObject o = reservedName.get();
				if(!o.has("discord") || !o.get("discord").getAsString().equals(discordUser.getId())) {
					return AuthState.RESERVED_NAME;
				}
			}

			else if (Classification.getClassification(name) != Classification.IRRELEVANT) {
				return AuthState.RESERVED_NAME;
			}
		}
		TMBotUser user = USERS_BY_NAME.get(name);
		if(user != null) {
			if(user.discord != null) {
				return AuthState.NAME_ALREADY_AUTHENTICATED;
			}
			//TODO: Validate using other modules
		} else {
			user = new TMBotUser(name);
			user.setClassification(Classification.getClassification(name));
		}
		initDiscord(user, discordUser);
		return AuthState.SUCCESS;
	}

	private static void initDiscord(TMBotUser user, User discordUser) {
		JsonObject data = new JsonObject();
		user.getData().add("discord", data);
		user.getData().addProperty("DISCORD_ID", discordUser.getId());
		user.discord = new DiscordUser(user, discordUser, data);
		mapUser(user);
	}

	private static void mapUser(TMBotUser user) {
		USERS_BY_NAME.put(user.getName().toLowerCase(), user);
		USERS_BY_SSN.put(user.getSSN().getSSN(), user);
		if(user.discord != null) {
			USERS_BY_DISCORD.put(user.discord.getBaseUser(), user);
		}
	}

	static void addAlias(String alias, TMBotUser user) {
		USERS_BY_ALIAS.put(alias.toLowerCase(), user);
	}

	static void removeAlias(String alias) {
		USERS_BY_ALIAS.remove(alias);
	}

	static Path getUserFile(TMBotUser user) {
		return USERS_DIR.resolve(user.getName() + ".json");
	}

	public static void save() throws IOException {
		Files.createDirectories(USERS_DIR);
		USERS_BY_NAME.forEach(Unchecked.biConsumer((n, u) -> {
			log.info(String.format("Saving user '%s'", n));
			Path file = getUserFile(u);
			if (!Files.exists(file)) {
				Files.createFile(file);
			}
			Files.write(file, Instances.getGson().toJson(u.getData()).getBytes(StandardCharsets.UTF_8));
			log.info(String.format("User '%s' saved", n));
		}));
	}

}
