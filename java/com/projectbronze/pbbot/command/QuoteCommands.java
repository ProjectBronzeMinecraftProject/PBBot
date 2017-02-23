package com.projectbronze.pbbot.command;

import static com.projectbronze.pbbot.Core.reply;
import java.util.Arrays;
import java.util.List;
import com.gt22.jdaenchacer.command.Command;
import com.gt22.jdaenchacer.command.ICommandList;
import com.projectbronze.pbbot.quotes.QuoteManager;
import com.projectbronze.pbbot.utils.MiscUtils;

public class QuoteCommands implements ICommandList
{

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(new Command[]{
				createCommand("цитата+", "цит+", "Добавляет цитату", "<Автор> <Цитата>", (msg, args, guild) ->
				{
					if (args.length < 2)
					{
						reply(msg, "Указаны неправельные аргументы");
					}
					String author = args[0];
					String[] newargs = new String[args.length - 1];
					for (int i = 1; i < args.length; i++)
					{
						newargs[i - 1] = args[i];
					}
					args = newargs;
					QuoteManager.add(author, msg.getAuthor().getName(), MiscUtils.getArrayAsString(args, " "));
					reply(msg, "Цитата добавлена");
				}, (msg, args, guild) -> reply(msg, "Ты не достоин!!"), 100),
				createCommand("цитату", "цит", "Показывает цитату", "[x:y]|[x:]|[:y]|[x]|[]", (msg, args, guild) ->
				{
					String ret = "";
					try
					{
						int size = 0;
						int quoteCount = QuoteManager.getQuotesCount();
						for (String s : args)
						{
							if (s.matches("^-*\\d*:-*\\d*$"))
							{
								String[] ends = s.split(":");
								int lend;
								int rend;
								if (s.matches("^-*\\d+:-*\\d+$"))
								{
									lend = Integer.parseInt(ends[0]) ;
									rend = Integer.parseInt(ends[1]);
								}
								else if (s.matches("^\\s*:-*\\d+$"))
								{
									lend = 0;
									rend = Integer.parseInt(ends[1]);
								}
								else if (s.matches("^-*\\d+:$"))
								{
									lend = Integer.parseInt(ends[0]) ;
									rend = quoteCount;
								}
								else
								{
									throw new NumberFormatException();
								}
								if (rend < lend)
								{
									reply(msg, String.format("Эмм %s меньше чем %s, поменяю их местами", rend, lend ));
									int tmp = lend ;
									lend = rend ;
									rend = tmp;
								}
								if (rend > quoteCount)
								{
									reply(msg, String.format("%s больше чем количество цитат, снижую до %s", rend, quoteCount));
									rend = quoteCount ;
								}
								if (lend < 0)
								{
									reply(msg, String.format("%s странное число, будет 1", lend));
									lend = 0;
								}
								if (rend < 1)
								{
									reply(msg, String.format("%s странное число, будет 1", rend));
									rend = 1;
								}
								size += rend - lend;
								ret += QuoteManager.getQuotes(lend, rend);
							}
							else
							{
								if(s.equals("*"))
								{
									break;
								}
								int pos = Integer.parseInt(s);
								if (pos >= quoteCount)
								{
									reply(msg, String.format("%s больше чем количество цитат, снижую до %s", pos, quoteCount));
									pos = quoteCount;
								}
								if (pos < 0)
								{
									reply(msg, String.format("%s странное число, будет 0", pos));
									pos = 0;
								}
								ret += QuoteManager.getQuote(pos);
								size++;
							}
						}
						if (size == 0)
						{
							int count = 1;
							if (args.length == 2 && args[0].equals("*"))
							{
								count = Integer.parseInt(args[1]);
							}
							ret += MiscUtils.getArrayAsString(QuoteManager.getQuotes(count), "\n");
							size = count;
						}
						if (size > 30)
						{
							String paste = MiscUtils.uploadToPastebin(ret, "Цитаты");
							if (!paste.startsWith("http"))
							{
								reply(msg, "Что-то пошло не так");
							}
							else
							{
								reply(msg, String.format("Цитаты: %s", paste));
							}
						}
						else
						{
							msg.getChannel().sendMessage("```" + ret + "```");
						}
					}
					catch (NumberFormatException e)
					{
						reply(msg, "Введено неверное число");
					}
				})
		});
	}
}
