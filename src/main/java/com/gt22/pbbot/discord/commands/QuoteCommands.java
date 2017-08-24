package com.gt22.pbbot.discord.commands;

import com.google.gson.JsonObject;
import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.utils.EmbedUtils;
import com.gt22.pbbot.utils.JavaHttpRequestBuilder;
import com.gt22.randomutils.Instances;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuoteCommands implements ICommandList {

	public static final AdvancedCategory cat = new AdvancedCategory("Quotes", new Color(0x434BAA), "https://maxcdn.icons8.com/1em/PNG/64/Messaging/speech_bubble_with_dots-64.png");
	private static final Pattern NFENumberExtract = Pattern.compile("For input string: \"(.*)\"");

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	@Override
	public Command[] init() {
		return new Command[]{
				command("quote", "add ore get quote", e -> {
					String args = e.getArgs();
					try {
						if (args.isEmpty()) { //Random
							getQuoteByPos(Instances.getRand().nextInt(getTotalQuotes()) + 1, e);
						} else if (args.matches("^\\d+$")) { //Position
							getQuoteByPos(Integer.parseInt(args), e);
						} else if (args.matches("^\\d+:\\d+$")) { //From : To
							String[] fromto = args.split(":", 2);
							getQuotesFromTo(fromto[0], fromto[1], e);
						} else if (args.matches("^\\* \\d+$")) { //Count
							int count = Integer.parseInt(args.substring(2));
							int total = getTotalQuotes();
							for (int i = 0; i < count; i++) {
								getQuoteByPos(Instances.getRand().nextInt(total) + 1, e);
							}
						} else {
							reply(e, 0xFF0000, "Invalid args", "Args should be '(add|(*none*|i%pos%|i%from%:i%to%|\\* i%count%))'", cat.getImg());
							e.reactWarning();
							return;
						}
						e.reactSuccess();
					} catch (IOException e1) {
						e.reply("Something went wrong: " + e1.getLocalizedMessage());
						e.reactError();
					} catch (NumberFormatException e1) {
						Matcher m = NFENumberExtract.matcher(e1.getMessage());
						if (m.matches()) {
							e.reply("Invalid number '" + m.group(1) + "'");
						} else {
							e.reply(e1.getMessage());
						}
						e.reactError();
					}
				}).setArguments("(add|(*none*|i%pos%|i%from%:i%to%|\\* i%count%))").setChildren(
						command("add", "adds quote", e -> {
							String[] args = e.getArgs().split("\\s+", 2);
							if (args.length != 2) {
								reply(e, 0xFF0000, "Invalid args", "Args should be '%author% %quote%'", cat.getImg());
								e.reactError();
							}
							try {
								JsonObject r = Instances.getParser().parse((new InputStreamReader(new JavaHttpRequestBuilder(Core.getConfig().QUOTER_URL)
										.addPostParam("task", "ADD")
										.addPostParam("addby", e.getMember().getUser().getName())
										.addPostParam("author", args[0])
										.addPostParam("quote", args[1])
										.addPostParam("key", Core.getConfig().QUOTER_KEY)
										.build().getInputStream()))).getAsJsonObject();
								if (r.get("error").getAsBoolean()) {
									e.reply(r.get("msg").getAsString());
									e.reactError();
								} else {
									e.reply("Added");
									e.reactSuccess();
								}
							} catch (IOException e1) {
								e.reply("Something went wrong: " + e1.getLocalizedMessage());
								e.reactWarning();
							}
						}).setRequiredPermission(90).setGuildOnly(false).setArguments("%author% %quote%").build()
				).build()
		};
	}

	private void getQuoteByPos(int pos, CommandEvent e) throws IOException {
		JsonObject rep = Instances.getParser().parse(new InputStreamReader(new JavaHttpRequestBuilder(Core.getConfig().QUOTER_URL)
				.addPostParam("task", "GET")
				.addPostParam("mode", "pos")
				.addPostParam("pos", Integer.toString(pos))
				.build().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
		sendQuote(rep, e, pos);
	}

	private void getQuotesFromTo(String from, String to, CommandEvent e) throws IOException {
		JsonObject rep = Instances.getParser().parse(new InputStreamReader(new JavaHttpRequestBuilder(Core.getConfig().QUOTER_URL)
				.addPostParam("task", "GET")
				.addPostParam("mode", "fromto")
				.addPostParam("from", from)
				.addPostParam("to", to)
				.build().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
		int[] i = {Integer.parseInt(from)};
		rep.getAsJsonArray("quotes").forEach(el -> sendQuote(el.getAsJsonObject(), e, i[0]++));
	}

	private void sendQuote(JsonObject quote, CommandEvent e, int id) {
		Instances.getExecutor().submit(() -> e.reply(new EmbedBuilder()
				.setColor(cat.getColor())
				.setThumbnail(cat.getImg())
				.addField(quote.get("author").getAsString() + ":", quote.get("quote").getAsString(), false)
				.setFooter("ID: " + id, null)
				.build()
		));
	}

	private int getTotalQuotes() throws IOException {
		JsonObject rep = Instances.getParser().parse(new InputStreamReader(new JavaHttpRequestBuilder(Core.getConfig().QUOTER_URL)
				.addPostParam("task", "GET")
				.addPostParam("mode", "total")
				.build().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
		if (rep.get("error").getAsBoolean()) {
			throw new RuntimeException("Something went wrong: " + rep.get("msg").getAsString());
		}
		return rep.get("count").getAsInt();
	}
}
