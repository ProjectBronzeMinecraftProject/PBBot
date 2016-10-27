package com.projectbronze.pbbot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class BashUtils
{
	public static final String BASH_QUOTE_START = "<div class=\"text\">", BASH_QUOTE_END = "</div>";
	public static String findQuote(BufferedReader bash) throws IOException
	{
		String tmp = bash.readLine();
		while(!tmp.contains(BASH_QUOTE_START))
		{
			tmp = bash.readLine();
		}
		while(!tmp.contains(BASH_QUOTE_END))
		{
			tmp += bash.readLine();
		}
		
		return tmp.substring(tmp.indexOf(BASH_QUOTE_START) + BASH_QUOTE_START.length(), tmp.indexOf(BASH_QUOTE_END));
	}
	
	public static URL getQuoute(int pos)
	{
		try
		{
			return new URL("http://bash.im/quote/" + pos);
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	public static int getQuotesCount()
	{
		try
		{
			URL bash = new URL("http://bash.im/");
			BufferedReader r = new BufferedReader(new InputStreamReader(bash.openStream(), "windows-1251"));
			String tmp = r.readLine();
			while(!tmp.contains("class=\"id\">#"))
			{
				tmp = r.readLine();
			}
			while(!tmp.contains("</a>"))
			{
				tmp += r.readLine();
			}
			tmp = tmp.substring(tmp.indexOf("class=\"id\">#") + "class=\"id\">#".length());
			return Integer.parseInt(tmp.substring(0, tmp.indexOf("</a>")));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
