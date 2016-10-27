package com.projectbronze.pbbot.quotes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import com.projectbronze.pbbot.nohub.NohubConst;

public class QuoteManager
{
	public static void add(String author, String adder, String quote)
	{
		upoloadToServer(adder, author, quote);
	}
	
	public static int getQuotesCount()
	{
		return Integer.parseInt(communicateToServer("http://projectbronze.eu.pn/php/citat.php", "mode=total"));
	}

	public static String getQuote(int pos)
	{
		return communicateToServer("http://projectbronze.eu.pn/php/citat.php", "mode=pos&pos=" + pos);
	}

	public static String[] getQuotes(int count)
	{
		int size = getQuotesCount();
		Random r = new Random();
		String[] ret = new String[count];
		for (int i = 0; i < count; i++)
		{
			ret[i] = getQuote(r.nextInt(size));
		}
		return ret;
	}

	public static String getQuotes(int from, int to)
	{
		return communicateToServer("http://projectbronze.eu.pn/php/citat.php", "mode=fromto&from=" + from + "&to=" + to);
	}

	public static void upoloadToServer(String adder, String author, String quote)
	{
		communicateToServer("http://projectbronze.eu.pn/php/add.php", ("addby=" + adder + "&author=" + author + "&citata=" + quote + "&key=" + NohubConst.ADD_QUOTE_KEY + "&bot=true"));
	}

	private static String communicateToServer(String url, String post)
	{
		try
		{
			byte[] bpost = post.getBytes();
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(bpost.length));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream()))
			{
				wr.write(bpost);
			}
			String ret = "";
			try(BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream())))
			{
				String tmp;
				while((tmp = r.readLine()) != null)
				{
					ret += tmp;
				}
			}
			return ret.replace("<br>", "\n").replace("<br />", "\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
