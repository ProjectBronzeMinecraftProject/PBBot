package com.gt22.pbbot.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.DiscordCore;
import com.gt22.pbbot.discord.DiscordUser;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.utils.ImageUtils;
import com.gt22.pbbot.utils.ManualFuture;
import com.gt22.randomutils.Instances;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.jooq.lambda.Unchecked;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TMBotUser {
	private static final SimpleLog log = SimpleLog.getLog("PBBot#TMBotUser");
	private final String name;
	DiscordUser discord;
	private SSN ssn;
	private JsonObject data;
	private Classification classification;
	private int level;

	TMBotUser(String name) {
		this.name = name;
		Path dataFile = Users.getUserFile(this);
		if (Files.exists(dataFile)) {
			loadData(dataFile);
		} else {
			loadDefault();
		}
		log.info(String.format("Creating TMBotUser %s", this));
	}

	private void loadData(Path dataFile) {
		try {
			data = Core.parse(dataFile).getAsJsonObject();
			if (data.has("SSN")) {
				ssn = new SSN(data.get("SSN").getAsInt());
			} else {
				ssn = SSN.randomSSN();
				updateSSN();
			}
			if (data.has("DISCORD_ID")) {
				JDA bot = DiscordCore.getBot();
				if (bot != null) {
					User discordUser = bot.getUserById(data.get("DISCORD_ID").getAsString());
					if (discordUser != null) {
						JsonObject discordData = data.getAsJsonObject("discord");
						if (discordData == null) {
							discordData = new JsonObject();
							data.add("discord", discordData);
						}
						discord = new DiscordUser(this, discordUser, discordData);
					}
				}
			}
			if (data.has("CLASSIFICATION")) {
				classification = Classification.getClassification(data.get("CLASSIFICATION").getAsString());
			} else {
				setClassification(Classification.getClassification(name));
			}
			if (data.has("ACCESS_LEVEL")) {
				level = data.get("ACCESS_LEVEL").getAsInt();
			}
			if (data.has("ALIASES")) {
				getAliases().forEach(e -> Users.addAlias(e.getAsString(), this));
			}
		} catch (IOException e) {
			log.fatal(String.format("Unable to load data for '%s'", name));
			log.log(e);
		}
	}

	private void loadDefault() {
		data = new JsonObject();
		Users.getReservedUser(name).ifPresent(info -> {
			if (info.has("vka")) {
				data.addProperty("VKA_ID", info.get("vka").getAsString());
			}
			if (info.has("level")) {
				setLevel(info.get("level").getAsInt());
			}
			if (info.has("ssn")) {
				ssn = new SSN(info.get("ssn").getAsInt());
				updateSSN();
			}
		});
		if (!data.has("ACCESS_LEVEL")) {
			setLevel(0);
		}
		if (!data.has("ssn")) {
			ssn = SSN.randomSSN();
		}
	}

	private void updateSSN() {
		data.addProperty("SSN", ssn.getSSN());
	}

	public DiscordUser getDiscordUser() {
		return discord;
	}

	public String getName() {
		return name;
	}

	public JsonObject getData() {
		return data;
	}

	public Classification getClassification() {
		return classification;
	}

	public void setClassification(Classification classification) {
		this.classification = classification;
		data.addProperty("CLASSIFICATION", classification.getName());
	}

	public void addAlias(String alias) {
		getAliases().add(alias);
		Users.addAlias(alias, this);
	}

	public void removeAlias(String alias) {
		JsonArray aliases = getAliases();
		JsonElement aliasPrimitive = new JsonPrimitive(alias);
		if (aliases.contains(aliasPrimitive)) {
			aliases.remove(aliasPrimitive);
			Users.removeAlias(alias);
		}
	}


	private JsonArray getAliases() {
		JsonArray aliases;
		if (!data.has("ALIASES")) {
			data.add("ALIASES", aliases = new JsonArray());
		} else {
			aliases = data.getAsJsonArray("ALIASES");
		}
		return aliases;
	}

	public List<String> getAllAliases() {
		JsonArray aliases = getAliases();
		List<String> ret = new ArrayList<>(aliases.size());
		aliases.forEach(e -> ret.add(e.getAsString()));
		return ret;
	}

	public String getVkAuthClientId() {
		return data.get("VKA_ID").getAsString();
	}

	public ManualFuture<String> getAvatarWithClassUrl() throws IOException {
		return getAvatarWithClassUrl(classification);
	}

	public ManualFuture<String> getAvatarWithClassUrl(Classification classification) throws IOException {
		ManualFuture<String> ret = new ManualFuture<>();
		Instances.getExecutor().submit(Unchecked.runnable(() -> ret.complete(EmbedUtils.convertImgToURL(Unchecked.supplier(() -> {
			BufferedImage avatar = discord == null ? new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB) : ImageUtils.readImg(discord.getEffectiveAvatarUrl()).get();
			BufferedImage frame = ImageUtils.readImg(classification.getImg(avatar == null ? 200 : avatar.getWidth())).get();
			return ImageUtils.mergeImages(avatar, frame);
		}), discord == null ? "EMPTY" : discord.getEffectiveAvatarUrl() + ":;:;:" + classification.getName()))));
		return ret;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		data.addProperty("ACCESS_LEVEL", level);
	}

	@Override
	public String toString() {
		return name + "#" + ssn;
	}

	public SSN getSSN() {
		return ssn;
	}
}
