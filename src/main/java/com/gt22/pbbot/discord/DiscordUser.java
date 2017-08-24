package com.gt22.pbbot.discord;

import com.google.gson.JsonObject;
import com.gt22.pbbot.user.TMBotUser;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.List;

public class DiscordUser implements User {

	private static final SimpleLog log = SimpleLog.getLog("PBBot#Discord#DiscordUser");

	/*private static final Map<String, DiscordUser> USERS = new HashMap<>();
	public static DiscordUser of(TMBotUser pbUser, User user) {
		return USERS.computeIfAbsent(user.getId(), k -> new DiscordUser(pbUser, user));
	}

	public static DiscordUser of(TMBotUser pbUser, User user, JsonObject data) {
		return USERS.computeIfAbsent(user.getId(), k -> new DiscordUser(pbUser, user, data));
	}

	public static void save() throws IOException {
		log.info("Saving users");
		Path users = Paths.get("internal", "users");
		Files.createDirectories(users);
		USERS.values().forEach(Unchecked.consumer(u -> Files.write(users.resolve(u.getId() + ".json"), u.getData().toString().getBytes(StandardCharsets.UTF_8))));
		log.info("Users saved");
	}*/

	private final TMBotUser pbUser;
	private final User user;
	private final JsonObject data;

	public DiscordUser(TMBotUser pbUser, User user, JsonObject data) {
		this.pbUser = pbUser;
		this.user = user;
		this.data = data;
		log.info(String.format("Creating DiscordUser %s", getIdentifier()));
	}

	/*private void loadData() throws IOException {
		Path file = Paths.get("internal", "users", user.getId() + ".json");
		if(Files.exists(file)) {
			log.info(String.format("Loading data of %s", getIdentifier()));
			data = Core.getParser().parse(Files.newBufferedReader(file, StandardCharsets.UTF_8)).getAsJsonObject();
		} else {
			log.info(String.format("Data not found for %s, creating new", getIdentifier()));
			data = new JsonObject();
			data.addProperty(LevelUtils.LEVEL_PROP_NAME, 0);
		}
	}*/

	@Override
	public String getName() {
		return user.getName();
	}

	@Override
	public String getDiscriminator() {
		return user.getDiscriminator();
	}

	@Override
	public String getAvatarId() {
		return user.getAvatarId();
	}

	@Override
	public String getAvatarUrl() {
		return user.getAvatarUrl();
	}

	@Override
	public String getDefaultAvatarId() {
		return user.getDefaultAvatarId();
	}

	@Override
	public String getDefaultAvatarUrl() {
		return user.getDefaultAvatarUrl();
	}

	@Override
	public String getEffectiveAvatarUrl() {
		return user.getEffectiveAvatarUrl();
	}

	@Override
	public boolean hasPrivateChannel() {
		return user.hasPrivateChannel();
	}

	@Override
	public RestAction<PrivateChannel> openPrivateChannel() {
		return user.openPrivateChannel();
	}

	@Override
	public List<Guild> getMutualGuilds() {
		return user.getMutualGuilds();
	}

	@Override
	public boolean isBot() {
		return user.isBot();
	}

	@Override
	public JDA getJDA() {
		return user.getJDA();
	}

	@Override
	public boolean isFake() {
		return user.isFake();
	}

	@Override
	public String getAsMention() {
		return user.getAsMention();
	}

	@Override
	public long getIdLong() {
		return user.getIdLong();
	}

	public TMBotUser getPbUser() {
		return pbUser;
	}

	public JsonObject getData() {
		return data;
	}

	public User getBaseUser() {
		return user;
	}

	public String getIdentifier() {
		return String.format("%s#%s", user.getName(), user.getDiscriminator());
	}

	@Override
	public String toString() {
		return "PBBot#DU " + getIdentifier();
	}
}
