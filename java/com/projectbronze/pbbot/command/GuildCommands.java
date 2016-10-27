package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.reply;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.data.AdvGuild;
import com.projectbronze.pbbot.utils.MiscUtils;
import com.projectbronze.pbbot.utils.Wrapper;
import net.dv8tion.jda.entities.TextChannel;

public class GuildCommands implements ICommandList
{

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(new Command[]{
				createCommand("блэклист+", "бллс+", "Добавляет канал в чёрный список, бот не будет реагировать на комманды из таких каналов", "<(ID|Имя) канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).addBlacklistedChannel(chan.single.get().getId());
							reply(msg, "Канал добавлен в чёрный список");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("блэклист-", "бллс-", "Убирает канал из чёрного списка", "<(ID|Имя) канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).removeBlacklistedChannel(chan.single.get().getId());
							reply(msg, "Канал убран из чёрного списка");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("блэклист", "бллс", "Показывает каналы в чёрном списке", "", (msg, args, guild) -> reply(msg, "```" + AdvGuild.of(guild).getBlacklistedChannels().getValue().parallelStream().map(d -> Core.bot.getTextChannelById((String) d.getValue()).getName()).reduce((s1, s2) -> s1 + "\n" + s2).orElse("Отсутствуют") + "```")),
				createCommand("вайтлист+", "вллс+", "Добавляет канал в белый список (Бот будет отвечать на сообщения только из таких каналов если есть хоть один)", "<(ID|Имя) канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).addWhitelistedChannel(chan.single.get().getId());
							reply(msg, "Канал добавлен в белый список");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("вайтлист-", "вллс-", "Убирает канал из белого списка", "<(ID|Имя) канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).removeWhitelistedChannel(chan.single.get().getId());
							reply(msg, "command.format.whitelist-.reply");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("вайтлист", "вллс", "Показывает каналы в белом списке", "", (msg, args, guild) -> reply(msg, "```" + AdvGuild.of(guild).getWhitelistedChannels().getValue().parallelStream().map(d -> Core.bot.getTextChannelById((String) d.getValue()).getName()).reduce((s1, s2) -> s1 + "\n" + s2).orElse("Отсутствуют") + "```")),
				createCommand("толькобот+", "тблс+", "Добавляет канал в список \"Только для бота\", бот будет удалять сообщения в таком канале, но будет реагировать на команлы", "<(ID|Имя)канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).addBotonlyChannel(chan.single.get().getId());
							reply(msg, "command.format.botonly+.reply");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("толькобот-", "тблс-", "Убирает канал из списка \"Только для бота\"", "<(ID|Имя)канала>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Укажите канал");
						return;
					}
					Wrapper<TextChannel> chan = MiscUtils.tryGetChannel(args[0], guild);
					switch(chan.state)
					{
						case SINGLE:
						{
							AdvGuild.of(guild).removeBotonlyChannel(chan.single.get().getId());
							reply(msg, "command.format.botonly-.reply");
							break;
						}
						case MULTI:
						{
							reply(msg, "Тут таких каналов много...");
							break;
						}
						case EMPTY:
						{
							reply(msg, "Хмммм... Не вижу такого канала");
							break;
						}
					}
				}, (msg, args, guild) -> reply(msg, "Не тебе мне фильтры ставить!!!"), 90),
				createCommand("толькобот", "тблс", "Показывает каналы в списке \"Только для бота\"", "", (msg, args, guild) -> reply(msg, "```" + AdvGuild.of(guild).getBotonlyChannels().getValue().parallelStream().map(d -> Core.bot.getTextChannelById((String) d.getValue()).getName()).reduce((s1, s2) -> s1 + "\n" + s2).orElse("Отсутствуют") + "```"))
		});
	}
}
