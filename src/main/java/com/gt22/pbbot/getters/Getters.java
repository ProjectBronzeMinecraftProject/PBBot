package com.gt22.pbbot.getters;

import com.gt22.pbbot.discord.DiscordCore;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Getters {

	private static final Pattern mention = Pattern.compile("<?@!?(\\d+)+>?");
	public static Wrapper<User> getUser(String from) {
		User user = null;
		Matcher matcher = mention.matcher(from);
		String name = matcher.matches() ? matcher.group(1) : from;
		if(name.matches("\\d+")) {
			user = DiscordCore.getBot().getUserById(name);
		}
		if(user == null) {
			List<User> users = DiscordCore.getBot().getUsersByName(name, true);
			if(users.isEmpty()) {
				users = DiscordCore.getBot().getGuilds().parallelStream().flatMap(g -> g.getMembersByEffectiveName(name, true).parallelStream()).distinct().map(Member::getUser).collect(Collectors.toList());
			}
			return new Wrapper<>(users);
		}
		return new Wrapper<>(user);
	}

}
