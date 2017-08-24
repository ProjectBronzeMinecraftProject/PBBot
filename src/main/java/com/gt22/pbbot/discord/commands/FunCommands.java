package com.gt22.pbbot.discord.commands;

import com.gt22.pbbot.Core;
import com.gt22.pbbot.discord.DiscordCore;
import com.gt22.pbbot.discord.commands.utils.ICommandList;
import com.gt22.pbbot.discord.misc.AdvancedCategory;
import com.gt22.pbbot.discord.music.MusicHandler;
import com.gt22.pbbot.utils.GifSequenceWriter;
import com.gt22.pbbot.utils.MiscUtils;
import com.gt22.randomutils.Instances;
import com.jagrosh.jdautilities.commandclient.Command;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunCommands implements ICommandList {

	public static final AdvancedCategory cat = new AdvancedCategory("Fun", new Color(0xAFEB73), "https://static1.varagesale.com/assets/emoji/grinning-cbe888b5b2231b7149ed5f635a9af5e13873a0cb608e0d001dcc5c6eaa635dca.png");
	private static final FacePart[] parts = new FacePart[]{new FacePart("<", ">"), new FacePart("(", ")"), new FacePart("-", "="), new FacePart("=", "="), new FacePart("=", "-")};
	private static final BufferedImage SAMARITAN_BLACK;
	private static final BufferedImage SAMARITAN_WHITE;
	private static final Font SAMARITAN_FONT = new Font("MagdaCleanMono", Font.PLAIN, 54);
	private static final Path VOICE_DIR = Paths.get("voice");
	private static final Pattern SAY_ARGS = Pattern.compile("(?:-r:(\\d+) )?(.+)");
	private static final HashMap<Integer, Character> NUMBERS_MAP = new HashMap<>();

	static {
		try {
			SAMARITAN_BLACK = ImageIO.read(Core.class.getResourceAsStream("/sam_black.png"));
			SAMARITAN_WHITE = ImageIO.read(Core.class.getResourceAsStream("/sam_white.png"));

			//Ubuntu versions
			NUMBERS_MAP.put(0, 'l');
			NUMBERS_MAP.put(1, 'n');
			NUMBERS_MAP.put(2, 'p');
			NUMBERS_MAP.put(3, 'r');
			NUMBERS_MAP.put(4, 'e');
			NUMBERS_MAP.put(5, 'v');
			NUMBERS_MAP.put(6, 'd');
			NUMBERS_MAP.put(7, 'f');
			NUMBERS_MAP.put(8, 'h');
			NUMBERS_MAP.put(9, 'j');
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public AdvancedCategory getCategory() {
		return cat;
	}

	@Override
	@SuppressWarnings("Костыль")
	public Command[] init() {
		return new Command[]{
				command("face", "creates random face", e -> {
					Random r = Instances.getRand();
					int len = r.nextInt(10) + 5;
					String start = generatePart(r, len).toString(), end = e.getArgs().contains("-s") ? swapParens(StringUtils.reverse(start)) : generatePart(r, len).toString();
					e.reply(start + "_" + end);
					e.reactSuccess();
				}).setGuildOnly(false).setArguments("[-s]").build(),
				command("samaritan", "Creates samaritan message", e -> {
					String msg = e.getArgs().toUpperCase();
					BufferedImage samaritan;
					Color textColor;
					boolean gif = false;
					boolean type = false;
					if (msg.contains("-W")) {
						msg = msg.replace("-W", "");
						samaritan = SAMARITAN_WHITE;
						textColor = Color.BLACK;
					} else if (msg.contains("-B")) {
						msg = msg.replace("-B", "");
						samaritan = SAMARITAN_BLACK;
						textColor = Color.WHITE;
					} else {
						reply(e, 0xFF0000, "No color specified", "Add -b (black) or -w (white) to your message to specify color");
						e.reactWarning();
						return;
					}
					if (msg.contains("-G")) {
						msg = msg.replace("-G", "");
						gif = true;
					}
					if (msg.contains("-T")) {
						if (gif) {
							msg = msg.replace("-T", "");
							type = true;
						} else {
							reply(e, 0xFF0000, "Cannot use 'Type'", "-t option can only be used together with -g");
						}
					}
					msg = msg.trim();
					if (msg.equals("%EMPTY%")) {
						msg = "";
					}
					File image = new File("tmp_samaritan." + (gif ? "gif" : "png"));
					//noinspection ResultOfMethodCallIgnored
					image.createNewFile();
					if (gif) {
						String[] words = msg.split(" ");
						ImageOutputStream out = new FileImageOutputStream(image);
						GifSequenceWriter gifWriter = new GifSequenceWriter(out, BufferedImage.TYPE_INT_ARGB, 700, true);
						if (type) {
							generateSamaritanTypingMessage(words, samaritan, textColor, gifWriter);
						} else {
							MiscUtils.ArrayUtils.forEach(words, Unchecked.consumer(word -> gifWriter.writeToSequence(createSamaritanMessage(word, samaritan, textColor))));
							gifWriter.writeToSequence(createSamaritanMessage("  ", samaritan, textColor));
						}
						gifWriter.close();
						out.close();
					} else {
						BufferedImage img = createSamaritanMessage(msg, samaritan, textColor);
						ImageIO.write(img, "png", image);
					}
					e.getChannel().sendFile(image, "samaritan." + msg.replace(",", "").replace(".", "") + (gif ? ".gif" : ".png"), null).queue(m -> {
						if (!image.delete()) {
							Core.getCoreLog().warn("Unable to delete tmp samaritan image");
						}
					});
					e.reactSuccess();

				}).setGuildOnly(false).setArguments("-(b|w) [-g [-t]] %message%").build(),

				command("say", "Bot pronounce specified text (Machine-encoded)", e -> {
					if (!e.getGuild().getMember(DiscordCore.getBot().getSelfUser()).getVoiceState().inVoiceChannel()) {
						reply(e, 0xFF0000, "Not in channel", "Bot must be in voice channel to use this command.\nUse 'sudo asd'");
						e.reactWarning();
						return;
					}
					Matcher m = SAY_ARGS.matcher(e.getArgs().toLowerCase());
					if (!m.matches()) {
						reply(e, 0xFF0000, "Invalid args", "Args should be '[-r:count] %message%'");
					}
					int repeats = m.group(1) == null ? 1 : Integer.parseInt(m.group(1));
					String msg = m.group(2).replace('-', '#');
					if (msg.isEmpty()) {
						reply(e, 0xFF0000, "No message", "No message specified");
						e.reactWarning();
						return;
					}
					char[] letters = msg.toCharArray();
					List<String> paths = new ArrayList<>(letters.length * 2 * repeats);
					System.out.println(repeats);
					for (int i = 0; i < repeats; i++) {
						for (char letter : letters) {
							if (letter == '#') {
								paths.add("#.mp3");
							} else {
								int digit = Character.digit(letter, 10);
								if (digit != -1) {
									letter = NUMBERS_MAP.get(digit);
								}
								paths.add((Instances.getRand().nextInt(3) + 1) + "/" + letter + ".mp3");
							}
						}
						paths.add("@.mp3");

					}
					Path tmpMsg = VOICE_DIR.resolve("tmp_message.mp3");
					if (Files.exists(tmpMsg)) {
						Files.delete(tmpMsg);
					}
					createMergedMp3(paths);
					MusicHandler.add(tmpMsg, e.getGuild(), false, false);
					e.reactSuccess();
				}).setArguments("[-r:i%count%] %message%").build(),
				command("root", "Everyone dies alone", e -> {
					MusicHandler.add(VOICE_DIR.resolve("root.mp3"), e.getGuild(), false, false);
					e.reactSuccess();
				}).setHidden().build()
		};
	}

	private void createMergedMp3(List<String> paths) throws IOException, InterruptedException {
		Process p = new ProcessBuilder("ffmpeg", "-i",
				MiscUtils.JoinUtils.join(paths, "|")
						.map(f -> String.format("concat:%s", f))
						.orElseThrow(() -> new IllegalArgumentException("At least one path must be provided")),
				"-c", "copy", "tmp_message.mp3")
				.directory(VOICE_DIR.toAbsolutePath().toFile())
				.start();
		p.getOutputStream().write("y\n".getBytes());
		p.getOutputStream().flush();
		p.waitFor();
	}

	private void generateSamaritanTypingMessage(String[] words, BufferedImage samaritan, Color textColor, GifSequenceWriter out) throws IOException {
		out.writeToSequence(createSamaritanMessage("", samaritan, textColor));
		StringBuilder builder = new StringBuilder();
		MiscUtils.ArrayUtils.forEach(words, Unchecked.consumer(word -> {
			out.writeToSequence(createSamaritanMessage(builder.append(word).toString(), samaritan, textColor));
			builder.append(' ');
		}));
	}

	private BufferedImage createSamaritanMessage(String msg, BufferedImage samaritan, Color textColor) {
		BufferedImage ret = new BufferedImage(samaritan.getWidth(), samaritan.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = ret.createGraphics();
		g.drawImage(samaritan, 0, 0, samaritan.getWidth(), samaritan.getHeight(), null);
		g.setPaint(textColor);
		g.setFont(SAMARITAN_FONT);
		FontMetrics m = g.getFontMetrics();
		int width = m.stringWidth(msg);
		int x = samaritan.getWidth() / 2 - width / 2;
		int y = 502;
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g.drawString(msg, x, y);
		int lineEnd = x + width + 5;
		g.drawLine(x - 10, 515, lineEnd, 515);
		g.drawLine(x - 10, 516, lineEnd, 516);
		g.dispose();
		return ret;
	}

	private String swapParens(String s) {
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(s.length()); // preallocate to prevent resizing
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case ')':
					sb.append('(');
					break;
				case '(':
					sb.append(')');
					break;
				case '<':
					sb.append('>');
					break;
				case '>':
					sb.append('<');
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();

	}

	private FacePart generatePart(Random r, int len) {
		int i = r.nextInt(parts.length);
		FacePart root = parts[i].copy(), next = root;
		for (int j = 0; j < len; j++) {
			FacePart p = parts[r.nextInt(parts.length)].copy();
			next.child = p;
			next = p;
		}
		return root;
	}

	private static class FacePart {
		String start;
		FacePart child;
		String end;

		FacePart(String start, String end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return start + (child == null ? "" : child.toString()) + end;
		}

		FacePart copy() {
			FacePart ret = new FacePart(start, end);
			ret.child = child == null ? null : child.copy();
			return ret;
		}
	}

}
