package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.bot;
import static com.projectbronze.pbbot.Core.reply;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.projectbronze.pbbot.music.MusicHandler;
import com.projectbronze.pbbot.music.MusicHandler.Return;
import com.projectbronze.pbbot.music.MusicHandler.State;
import com.projectbronze.pbbot.utils.FormatUtils;
import com.projectbronze.pbbot.utils.MiscUtils;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.managers.AudioManager;

public class SoundCommands implements ICommandList{

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(new Command[]{
				createCommand("комне", "фыв", "бот приходит к вам в канал", "[--hard]", (msg, args, guild) ->
				{
					User sender = msg.getAuthor();
					VoiceChannel userChannel = guild.getVoiceStatusOfUser(sender).getChannel();
					AudioManager botman = bot.getAudioManager(guild);
					boolean hard = false;
					if (args.length != 0)
					{
						hard = args[0].equals("--hard") || args[0].equals("-h") || args[0].equals("-х");
					}
					if (!hard && userChannel == guild.getVoiceStatusOfUser(bot.getSelfInfo()).getChannel())
					{
						reply(msg, "Я и так тут -_-");
						return;
					}
					if (userChannel == null)
					{
						reply(msg, "Куда это?");
						return;
					}
					if (botman.isConnected() || botman.isAttemptingToConnect())
					{
						botman.closeAudioConnection();
					}
					botman.openAudioConnection(userChannel);
					reply(msg, "Я тута");
				}, (msg, args, guild) ->
				{
					reply(msg, "Не-а");
				}, 50),
				createCommand("уйди", "выф", "Бот уходит из канала", "", (msg, args, guild) ->
				{
					AudioManager botman = guild.getAudioManager();
					if (botman.getConnectedChannel() == null)
					{
						reply(msg, "Я не в канале -_-");
						return;
					}
					botman.closeAudioConnection();
				}, (msg, args, guild) ->
				{
					reply(msg, "Я тебе не повинуюсь");
				}, 50),
				createCommand("музыку", "мзк", "Добавляет музыку в плэйлист", "[Имя|Ссылка][* x]", (msg, args, guild) ->
				{
					if (!MiscUtils.isInChannel(guild))
					{
						reply(msg, "Я не в канале");
						return;
					}
					List<String> resultArgs = new ArrayList<String>();
					int count = 1;
					boolean skipArg = false, all = false;
					for (int i = 0; i < args.length; i++)
					{
						switch (args[i].toLowerCase())
						{
							case ("*"):
							{
								if (args.length < i)
								{
									reply(msg, "Укажите количество");
									return;
								}
								try
								{
									count = Integer.parseInt(args[i + 1]);
									skipArg = true;
								}
								catch (NumberFormatException e)
								{
									reply(msg, "Введено не правильное количество песен");
									return;
								}
								break;
							}
							case ("-a"):
							case ("--all"):
							{
								all = true;
								break;
							}
							default:
							{
								if (!skipArg)
								{
									resultArgs.add(args[i]);
								}
							}
						}
					}
					args = resultArgs.toArray(new String[resultArgs.size()]);
					switch (args.length)
					{
						case (0):
						{
							Random r = new Random();
							for (int i = 0; i < count; i++)
							{
								String music = all ? "" : MiscUtils.getRandomFile(MusicHandler.musicDir, new Random()).getPath().replace("\\", "/");
								MusicHandler.add(music.substring(music.indexOf('/') + 1), 0.05F, true, guild, all, r);
							}
							reply(msg, "Музыка добавлена в плэйлист");
							break;
						}
						default:
						{
							Return result = Return.NOFILE;
							Random r = new Random();
							for (int i = 0; i < count; i++)
							{
								result = MusicHandler.add(MiscUtils.getArrayAsString(args, " "), 0.05F, !args[0].startsWith("http"), guild, all, r);
							}
							switch (result)
							{
								case NOFILE:
								{
									reply(msg, "Я не знаю такой музыки");
									break;
								}
								case NOURL:
								{
									reply(msg, "Ссылка какая-то ммм... Странная...");
									break;
								}
								case OK:
								{
									reply(msg, "Музыка добавлена в плэйлист");
									break;
								}

							}
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не буду я тебя слущать."), 30),
				/*createCommand("пауза", "пз", "Останавливает музыку", (msg, args, guild) ->
				{
					if (MusicHandler.getState() != State.PLAYING)
					{
						reply(msg, "Музыка не играет");
					}
					else
					{
						MusicHandler.pause();
						reply(msg, "Всё теперь на паузе");
					}
				});
				createCommand("пуск", "пу", "возобновляет музыку после паузы", (msg, args, guild) ->
				{
					if (MusicHandler.getState() != State.PAUSE)
					{
						reply(msg, "Музыка не на паузе");
					}
					else
					{
						MusicHandler.resume();
						reply(msg, "Включаю");
					}
				}, (msg, args, guild) ->
				{
					reply(msg, "Сиди жди, ~~смертный~~ юзер");
				}, 10);*/
				createCommand("пропустить", "скип", "Пропускает музыку с указаным номером, если номер не указан пропускает текущию", "[номер]", (msg, args, guild) ->
				{
					if (MusicHandler.getState() != State.PLAYING)
					{
						reply(msg, "Музыка не играет");
					}
					else
					{
						int toSkip = 1;
						if (args.length != 0)
						{
							try
							{
								toSkip = Integer.parseInt(args[0]);
							}
							catch (NumberFormatException e)
							{
								reply(msg, "Указан неверный номер музыки");
							}
						}
						MusicHandler.skip(toSkip);
						reply(msg, "Есть");
					}
				}, (msg, args, guild) ->
				{
					reply(msg, "Слушай раз не админ");
				}, 10),
				createCommand("всямузыку", "вмзк", "Показывает список всей известной музыки", "", (msg, args, guild) ->
				{
					String paste = MiscUtils.uploadToPastebin(FormatUtils.formatDir(MusicHandler.musicDir, false, 0), "Музыка которую я знаю");
					if (!paste.startsWith("http"))
					{
						reply(msg, "Что-то пошло не так, напишите об этом автору");
					}
					else
					{
						reply(msg, "Музыка которую я знаю: " + paste);
					}
				}),
				createCommand("сброс", "рс", "Отчищает плэйлист", "", (msg, args, guild) ->
				{
					MusicHandler.reset();
					reply(msg, "Ресетнуто");
				}, (msg, args, guild) ->
				{
					reply(msg, "ТЫ НЕДОСТОИН");
				}, 10),
				createCommand("плэйлист", "пллс", "Показывает плэйлист", "[x:y]|[x:]|[:y]|[x]|[]", (msg, args, guild) -> {
					String ret = "Плэйлист:\n";
					int size = 0;
					if (MusicHandler.playlistSize() == 0) {
						reply(msg, "Плэйлист пуст");
						return;
					}
					try {
						for (String s : args) {
							if (s.matches("^-*\\d*:-*\\d*$")) {
								String[] ends = s.split(":");
								int lend;
								int rend;
								if (s.matches("^-*\\d+:-*\\d+$")) {
									lend = Integer.parseInt(ends[0]) - 1;
									rend = Integer.parseInt(ends[1]);
								} else if (s.matches("^\\s*:-*\\d+$")) {
									lend = 0;
									rend = Integer.parseInt(ends[1]);
								} else if (s.matches("^-*\\d+:$")) {
									lend = Integer.parseInt(ends[0]) - 1;
									rend = MusicHandler.playlistSize();
								} else {
									throw new NumberFormatException();
								}
								if (rend < lend) {
									reply(msg, "Эмм... " + rend + " меньше чем " + (lend + 1) + ", я их пожалуй местами помен¤ю");
									int tmp = lend + 1;
									lend = rend - 1;
									rend = tmp;
								}
								if (rend > MusicHandler.playlistSize()) {
									reply(msg, rend + " больше чем размер плэйлиста, снижаю до " + MusicHandler.playlistSize());
									rend = MusicHandler.playlistSize() - 1;
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
									ret += FormatUtils.formatSong(i);
								}
							} else {
								int pos = Integer.parseInt(s) - 1;
								if (pos >= MusicHandler.playlistSize()) {
									reply(msg, pos + " больше чем размер плэйлиста, снижаю до " + MusicHandler.playlistSize());
									pos = MusicHandler.playlistSize() - 1;
								}
								if (pos < 0) {
									reply(msg, (pos + 1) + " странное число, пусть лучше будет 1");
									pos = 0;
								}
								ret += FormatUtils.formatSong(pos);
								size++;
							}
						}
						if (size == 0) {
							ret = FormatUtils.formatPlaylist();
							size = MusicHandler.playlistSize() - 1;
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

}
