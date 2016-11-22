package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.reply;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.gt22.jdaenchacer.data.AdvUser;
import com.gt22.jdaenchacer.data.tags.DataStorage;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.utils.FormatUtils;
import com.projectbronze.pbbot.utils.MiscUtils;
import com.projectbronze.pbbot.utils.Wrapper;

import net.dv8tion.jda.entities.User;

public class UtilsCommands implements ICommandList
{

	public static class Departure
	{
		public static final SimpleDateFormat form = new SimpleDateFormat("HH:mm");
		public String departedIn;
		public int departedFor; // Minutes
		public String departedFrom;//Guild id
		public Departure(String departedIn, int departedFor, String departedFrom)
		{
			this.departedIn = departedIn;
			this.departedFor = departedFor;
			this.departedFrom = departedFrom;
		}

		public DataStorage toData()
		{
			DataStorage ret = new DataStorage();
			ret.setString("in", departedIn);
			ret.setInt("for", departedFor);
			ret.setString("from", departedFrom);
			return ret;
		}

		public static Departure fromData(DataStorage s)
		{
			return s == null ? null : new Departure(s.getString("in"), s.getInt("for"), s.getString("from"));
		}

	}

	public void init()
	{
		
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(new Command[]{
				createCommand("умри", "умр", "Убивает бота", "", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						Core.exit(0, null);
					}
					else
					{
						Core.exit(Integer.parseInt(args[0]), null);
					}
				}, (msg, args, guild) -> reply(msg, "ТЫ МЕНЯ НЕ ПОБЕДИШЬ!!!"), 100),
				createCommand("пинг", "NONE", "Тестирует пинг до бота (Иногда бот вне времени)", "", (msg, args, guild) ->
				{
					long msgTime = msg.getTime().atZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(3))).toInstant().toEpochMilli();
					long botTime = Calendar.getInstance().getTime().getTime();
					SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss:SS");
					reply(msg, "Pong, Сообщение получено в %s, отправленно в %s\nРазница в милисекундах: %s", format.format(botTime), format.format(msgTime), botTime - msgTime);
				}),
				createCommand("ребут", "рб", "Бот перезагружает команды", "", (msg, args, guild) ->
				{
						reply(msg, "Перезагружаю команды");
						Core.commands.reset();
				}, (msg, args, guild) ->
				{
					reply(msg, "Ты меня не перезагрузишь!!!");
				}, 70),
				createCommand("команды", "хэлп", "Показывает команды, если указана команда показывает информацию о ней. Хотя вы ведь и так это знаете?", "[команда|-синтаксис]", (msg, args, guild) ->
				{
					if (args.length != 0)
					{
						if (args[0].equals("-синтаксис"))
						{
							reply(msg, "<обязательные параметры> [опциональные параметры], | - один из перечисленных параметров, если перед параметром есть -- или - он должен быть передан прямым текстом");
						}
						else
						{
							Command cm = Core.commands.getCommandByName(args[0]);
							if (cm == null)
							{
								reply(msg, "Не могу найти такой комманды, ты мне какую-то **~~дичь~~** лажу дал.");
							}
							else
							{
								msg.getChannel().sendMessage(FormatUtils.formatHelp(cm));
							}
						}
					}
					else
					{
						msg.getChannel().sendMessage(FormatUtils.formatHelp());
					}
				}),
				createCommand("отошёл", "отш", "Бот мутит вас до следующего сообщения, если указан аргумент показывает информацию о том когда отощёл челове", "<време>|<--info Имя>", (msg, args, guild) ->
				{
					boolean info = false;
					String name = "";
					for (String s : args)
					{
						switch (s)
						{
							case ("--info"):
							case ("-i"):
							{
								info = true;
								break;
							}
							default:
							{
								if (info)
								{
									name += s + " ";
								}
							}
						}
					}
					if (!info)
					{
						AdvUser usr = AdvUser.of(msg.getAuthor());
						try
						{
							int time;
							usr.setData("dep", new Departure(MiscUtils.getTime(Departure.form), time = Integer.parseInt(args[0]), guild.getId()).toData());
							reply(msg, String.format("Вы отошли на %s минут", time));
							guild.getManager().mute(msg.getAuthor());
						}
						catch (NumberFormatException e)
						{
							reply(msg, "Введено не корректное время");
							return;
						}
					}
					else
					{
						name = name.trim();
						Departure dep;
						Wrapper<User> w = MiscUtils.tryGetUser(name);
						switch (w.state)
						{
							case SINGLE:
							{
								dep = Departure.fromData(AdvUser.of(w.single.get()).getTag("dep"));
								break;
							}
							default:
							{
								reply(msg, "Не совсем понимаю о ком вы");
								return;
							}
						}
						if (dep != null)
						{
							reply(msg, String.format("%s отошёл в %s на %s минут", name, dep.departedIn, dep.departedFor));
						}
						else
						{
							reply(msg, "Этот человек не отходил");
						}
					}
				}),
				createCommand("шестерёнки", "gears", "Бот отправляет шестерёнки", "", (msg, args, guild) ->
				{
					reply(msg, "http://projectbronze.comli.com/test/gears.jpg");
				}),
				createCommand("баш.им", "баш", "Присылает цитату с баша", "[* x]", (msg, args, guild) ->
				{
					int count = 1;
					argsLoop:
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
									break argsLoop;
								}
								catch (NumberFormatException e)
								{
									reply(msg, "Введено не правильное количество цитат");
									return;
								}
							}
						}
					}
					Random r = new Random();
					for (int i = 0; i < count; i++)
					{
						msg.getChannel().sendMessage(FormatUtils.formatBash(r));
					}
				}),
				createCommand("скажи", "say", "Боты отправляет в чат аргументы этой команды", "[что, сказать, боту]", (msg, args, guild) -> {
					String rep = "";
					for(String s : args)
					{
						rep += s;
					}
					msg.getChannel().sendMessage(rep);
				}, (msg, args, guild) -> {
					reply(msg, "Ну неееет.");
				}, 100)
		});
	}
}
