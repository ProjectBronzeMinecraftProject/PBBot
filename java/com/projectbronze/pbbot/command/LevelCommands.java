package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.reply;
import java.util.Arrays;
import java.util.List;
import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.projectbronze.pbbot.utils.FormatUtils;
import com.projectbronze.pbbot.utils.LevelUtils;
import com.projectbronze.pbbot.utils.MiscUtils;
import com.projectbronze.pbbot.utils.Wrapper;
import com.projectbronze.pbbot.utils.Wrapper.WrapperState;
import net.dv8tion.jda.entities.User;

public class LevelCommands implements ICommandList
{

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(new Command[]{
				createCommand("админ+", "адм+", "Добавляет администратора", "<Имя|ID> <Уровень>", (msg, args, guild) ->
				{
					if (args.length != 2)
					{
						reply(msg, "Указаны неправельные аргументы");
						return;
					}
					User usr = null;
					Wrapper<User> users = MiscUtils.tryGetUser(args[0]);
					if (users.state == WrapperState.SINGLE)
					{
						usr = users.single.get();
					}
					else
					{
						reply(msg, "Не могу точно понять кого вы хотите добавить, думаю вам стоит указывать одного юзера.");
						return;
					}
					try
					{
						int level = Integer.parseInt(args[1]), senderLevel = LevelUtils.getLevel(msg.getAuthor());
						if (level < 1)
						{
							reply(msg, "Что это за админ с допуском ниже 1???");
							return;
						}
						if(level > senderLevel)
						{
							reply(msg, "Ты не достоин таких крутых админов добавлять");
							return;
						}
						if(LevelUtils.getLevel(usr) >= senderLevel)
						{
							reply(msg, "Не дам управлять теми кто круче тебя");
							return;
						}
						LevelUtils.addAdmin(usr, level);
					}
					catch (NumberFormatException e)
					{
						reply(msg, "Указан невозможный уровень");
					}
					reply(msg, usr.getUsername() + "#" + usr.getDiscriminator() + " теперь админ");
				}, (msg, args, guild) ->
				{
					reply(msg, "Ты сам не админ, а других пытаешся добавить?");
				}, 1),
				createCommand("админ-", "адм-", "Убирает администратора", "<Имя|ID>", (msg, args, guild) ->
				{
					User usr = null;
					Wrapper<User> users = MiscUtils.tryGetUser(args[0]);
					if (users.state == WrapperState.SINGLE)
					{
						usr = users.single.get();
					}
					else
					{
						reply(msg, "Не могу точно понять кого вы хотите ~~свергнуть~~ убрать,  думаю вам стоит @упоминать одного юзера.");
						return;
					}
					LevelUtils.removeAdmin(usr);
					reply(msg, usr.getUsername() + "#" + usr.getDiscriminator() + " больше не админ");
				}, (msg, args, guild) ->
				{
					reply(msg, "Не позволю админов трогать!");
				}, 100),
				createCommand("заблокировать", "блок+", "Блокирует пользователя (Бот будет удалять его сообщения и не реагировать на его команды)", "<Имя|ID>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Вы не указали кого хотите заблокировать");
						return;
					}
					User usr = null;
					Wrapper<User> users = MiscUtils.tryGetUser(args[0]);
					if (users.state == WrapperState.SINGLE)
					{
						usr = users.single.get();
					}
					else
					{
						reply(msg, "Не могу точно понять кого вы хотите ~~убить~~ заблокировать,  думаю вам стоит указывать одного юзера.");
						return;
					}
					LevelUtils.block(usr);
					reply(msg, usr.getUsername() + "#" + usr.getDiscriminator() + " теперь заблокирован");
				}),
				createCommand("разблокировать", "блок-", "Снимает блокировку с пользователя", "<Имя|ID>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Вы не указали кого хотите разблокировать");
						return;
					}
					User usr = null;
					Wrapper<User> users = MiscUtils.tryGetUser(args[0]);
					if (users.state == WrapperState.SINGLE)
					{
						usr = users.single.get();
					}
					else
					{
						reply(msg, "Не могу точно понять кого вы хотите разблокировать,  думаю вам стоит указывать одного юзера.");
						return;
					}
					LevelUtils.unblock(usr);
					reply(msg, usr.getUsername() + "#" + usr.getDiscriminator() + " теперь разблокирован");
				}),
				createCommand("админы", "адм", "Показывает администраторов", "", (msg, args, guild) ->
				{
					reply(msg, FormatUtils.formatAdmins());
				}),
				createCommand("заблокированные", "блок", "Показывает заблокированных пользователей", "", (msg, args, guild) ->
				{
					reply(msg, "```Заблокированые:\n%s\n```", FormatUtils.fortmatBlocked());
				}),
				createCommand("информация", "инфа", "Показывает информацию о пользователе", "<Имя|ID>", (msg, args, guild) ->
				{
					if (args.length == 0)
					{
						reply(msg, "Пожалуйсто укажите пользователя");
						return;
					}
					Wrapper<User> usr = MiscUtils.tryGetUser(args[0]);
					switch (usr.state)
					{
						case EMPTY:
						{
							reply(msg, "Не могу нати такого пользователя");
							return;
						}
						case SINGLE:
						{
							reply(msg, FormatUtils.formatUser(usr.single.get(), guild));
							return;
						}
						case MULTI:
						{
							String info = "";
							User[] users = usr.multipile.get();
							for (User u : users)
							{
								info += FormatUtils.formatUser(u, guild) + "\n";
							}
							reply(msg, "Найдено несколько подходящих пользователей, вывожу информацию о каждом:\n" + info);
						}
					}
				})
		});
	}
}
