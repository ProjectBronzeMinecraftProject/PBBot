package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.reply;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.music.MusicHandler;
import com.projectbronze.pbbot.music.MusicHandler.Return;
import com.projectbronze.pbbot.utils.FormatUtils;
import com.projectbronze.pbbot.utils.MiscUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;


public class SoundCommands implements ICommandList {

	@Override
	public List<Command> getCommands() {
		//@formatter:off
		return Arrays.asList(new Command[] { 
			createCommand("комне", "фыв", "бот приходит к вам в канал", "[--hard]", (msg, args, guild) -> {
				Member sender = guild.getMember(msg.getAuthor());
				VoiceChannel userChannel = sender.getVoiceState().getChannel();
				AudioManager botman = guild.getAudioManager();
				boolean hard = false;
				if (args.length != 0) {
					hard = args[0].equals("--hard") || args[0].equals("-h") || args[0].equals("-х");
				}
				if (!hard && userChannel == guild.getMember(Core.bot.getSelfUser()).getVoiceState().getChannel()) {
					reply(msg, "Я и так тут -_-");
					return;
				}
				if (userChannel == null) {
					reply(msg, "Куда это?");
					return;
				}
				if (botman.isConnected() || botman.isAttemptingToConnect()) {
					botman.closeAudioConnection();
				}
				botman.openAudioConnection(userChannel);
				reply(msg, "Я тута");
			}, (msg, args, guild) -> reply(msg, "Не-а"), 50),
			createCommand("уйди", "выф", "Бот уходит из канала", "", (msg, args, guild) -> {
				AudioManager botman = guild.getAudioManager();
				if (botman.getConnectedChannel() == null) {
					reply(msg, "Я не в канале -_-");
					return;
				}
				botman.closeAudioConnection();
			}, (msg, args, guild) -> reply(msg, "Я тебе не повинуюсь"), 50), 
			createCommand("музыку", "мзк", "Добавляет музыку в плэйлист", "[Имя|Ссылка][* x]", (msg, args, guild) -> {
				if (!MiscUtils.isInChannel(guild)) {
					reply(msg, "Я не в канале");
					return;
				}
				List<String> resultArgs = new ArrayList<String>();
				int count = 1;
				boolean skipArg = false, all = false, noRepeat = false;
				for (int i = 0; i < args.length; i++) {
					switch (args[i].toLowerCase()) {
						case ("*"): {
							if (args.length > i + 1) {
								try {
									count = Integer.parseInt(args[i + 1]);
									skipArg = true;
								} catch (NumberFormatException e) {
									reply(msg, "Введено не правильное количество песен");
									return;
								}
								break;
							} else {
								reply(msg, "Укажите количество");
								return;
							}
						}
						case ("-a"):
						case ("--all"): {
							all = true;
							break;
						}
						case ("-nr"):
						case ("-бп"):
						case ("--no-repeat"): {
							noRepeat = true;
							break;
						}
						default: {
							if (!skipArg) {
								resultArgs.add(args[i]);
							}
						}
					}
				}
				args = resultArgs.toArray(new String[resultArgs.size()]);
				if (args.length == 0) {
					processMusicHandlerReturn(msg, MusicHandler.addXTimes(MusicHandler.musicDir, count, noRepeat, all, guild));
				} else {
					String path = MiscUtils.getArrayAsString(args, " ");
					System.out.println(path + ":" + path.startsWith("http"));
					if (path.startsWith("http")) {
						try {
							processMusicHandlerReturn(msg, MusicHandler.addXTimes(new URL(path), count, noRepeat, guild));
						} catch (MalformedURLException e) {
							reply(msg, "Ссылку а вас не той системы");
						}
					} else {
						processMusicHandlerReturn(msg, MusicHandler.addXTimes(new File(MusicHandler.musicDir, path + ".mp3"), count, noRepeat, all, guild));
					}
				}
			}, (msg, args, guild) -> reply(msg, "Не буду я тебя слушать."), 30),
			createCommand("пауза", "пз", "Останавливает музыку", "", (msg, args, guild) -> {
				if (MusicHandler.getManager(guild).player.getPlayingTrack().getState() != AudioTrackState.PLAYING) {
					reply(msg, "Музыка не играет");
				} else {
					MusicHandler.pause(guild);
					reply(msg, "Всё теперь на паузе");
				}
			}),
			createCommand("пуск", "пу", "возобновляет музыку после паузы", "", (msg, args, guild) -> {
				if (!MusicHandler.getManager(guild).player.isPaused()) {
					reply(msg, "Музыка не на паузе");
				} else {
					MusicHandler.resume(guild);
					reply(msg, "Включаю");
				}
			}, (msg, args, guild) -> reply(msg, "Сиди жди, ~~смертный~~ юзер"), 10),
			createCommand("пропустить", "скип", "Пропускает музыку с указаным номером, если номер не указан пропускает текущию", "[номер]", (msg, args, guild) -> {
				if (MusicHandler.playlistSize(guild) == 0) {
					reply(msg, "Музыка не играет");
				} else {
					int toSkip = 1;
					if (args.length != 0) {
						try {
							toSkip = Integer.parseInt(args[0]);
						} catch (NumberFormatException e) {
							reply(msg, "Указан неверный номер музыки");
						}
					}
					MusicHandler.skip(toSkip, guild);
					reply(msg, "Есть");
				}
			}, (msg, args, guild) -> reply(msg, "Слушай раз не админ"), 10), 
			createCommand("всямузыку", "вмзк", "Показывает список всей известной музыки", "", (msg, args, guild) -> {
				String paste = MiscUtils.uploadToPastebin(FormatUtils.formatDir(MusicHandler.musicDir, false, 0), "Музыка которую я знаю");
				if (!paste.startsWith("http")) {
					reply(msg, "Что-то пошло не так, напишите об этом автору");
				} else {
					reply(msg, "Музыка которую я знаю: " + paste);
				}
			}),
			createCommand("сброс", "рс", "Отчищает плэйлист", "", (msg, args, guild) -> {
				MusicHandler.reset(guild);
				reply(msg, "Ресетнуто");
			}, (msg, args, guild) -> reply(msg, "ТЫ НЕДОСТОИН"), 10),
			createCommand("плэйлист", "пллс", "Показывает плэйлист", "[x:y]|[x:]|[:y]|[x]|[]", (msg, args, guild) -> {
				String ret = "Плэйлист:\n";
				int size = 0;
				int plsize = MusicHandler.playlistSize(guild);
				if (plsize == 0) {
					reply(msg, "Плэйлист пуст");
					return;
				}
				try {
					for (String s : args) {
						if (s.matches("^-?\\d*:-?\\d*$")) {
							String[] ends = s.split(":");
							int lend;
							int rend;
							if (s.matches("^-?\\d+:-?\\d+$")) {
								lend = Integer.parseInt(ends[0]) - 1;
								rend = Integer.parseInt(ends[1]);
							} else if (s.matches("^\\s*:-?\\d+$")) {
								lend = 0;
								rend = Integer.parseInt(ends[1]);
							} else if (s.matches("^-?\\d+:$")) {
								lend = Integer.parseInt(ends[0]) - 1;
								rend = plsize;
							} else {
								throw new NumberFormatException();
							}
							if (lend + 1 > rend) {
								reply(msg, "Эмм... " + (lend + 1) + " больше чем " + rend + ", я их пожалуй местами поменяю");
								int tmp = lend + 1;
								lend = rend - 1;
								rend = tmp;
							}
							if (rend > plsize) {
								reply(msg, rend + " больше чем размер плэйлиста, снижаю до " + plsize);
								rend = plsize;
							}
							if (lend < 0) {
								reply(msg, (lend + 1) + " странное число, пусть лучше будет 1");
								lend = 0;
							}
							if (rend < 0) {
								reply(msg, rend + " странное число, пусть лучше будет 1");
								rend = 1;
							}
							size += rend - lend;
							for (int i = lend; i < rend; i++) {
								ret += FormatUtils.formatSong(i, guild);
							}
						} else if (s.equals("rand") || s.equals("ранд")) {
							size = 1;
							ret = "```" + FormatUtils.formatSong(Core.rand.nextInt(plsize), guild) + "```";
						} else {
							int pos = Integer.parseInt(s);
							if (pos >= plsize) {
								reply(msg, pos + " больше чем размер плэйлиста, снижаю до " + plsize);
								pos = plsize - 1;
							}
							if (pos < 0) {
								reply(msg, (pos + 1) + " странное число, пусть лучше будет 1");
								pos = 0;
							}
							ret += FormatUtils.formatSong(pos, guild);
							size++;
						}
					}
					if (size == 0) {
						ret = FormatUtils.formatPlaylist(guild);
						size = plsize - 1;
					}
					if (size > 30) {
						String paste = MiscUtils.uploadToPastebin(ret, "Плэйлист");
						if (!paste.startsWith("http")) {
							reply(msg, "Что-то пошло не так, напишите об этом автору");
						} else {
							reply(msg, "Плэйлист: " + paste);
						}
					} else {
						reply(msg, ret);
					}
				} catch (NumberFormatException e) {
					reply(msg, "Введено неверное число");
				}
			})
		});
	}

	private static void processMusicHandlerReturn(Message msg, Return ret) {
		switch (ret) {
			case INVALID_FILE: {
				reply(msg, "Не могу я такую музыку играть");
				break;
			}
			case NOFILE: {
				reply(msg, "Не могу найти у себя такую музыку, возможно вам стит написать !всямузыка?");
				break;
			}
			case NOTFILE: {
				reply(msg, "Эммммм... ЭТОГО НЕ ДОЛЖНО БЫЛО ПРОИЗОЙТИ!!!! ПИШЕТЕ РАЗРАБОТЧИКУ!!! СРОЧНО!!!");
				break;
			}
			case OK: {
				reply(msg, "Музыка добавленя в плэйлист");
				break;
			}
			case CANNOT_ADD: {
				reply(msg, "У меня закончились песни(");
			}
		}
	}

}
