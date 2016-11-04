package com.projectbronze.pbbot.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gt22.jdaenchacer.utils.FileUtils;	

class Logger
{
	private static File logFolder, logFile;
	private static boolean inited = false;

	public static void init() throws IOException
	{
		if (!inited)
		{
			logFolder = new File("Logs");
			logFile = new File(logFolder, "BotLog-0.log");
			logFolder.mkdirs();
			processFiles();
			inited = true;
		}
	}

	// Deletes old logs, and renames new
	private static void processFiles() throws IOException
	{
		File log3 = new File(logFolder, "BotLog-3.log");
		if (log3.exists())
		{
			log3.delete();
		}
		File log2 = new File(logFolder, "BotLog-2.log");
		if (log2.exists())
		{
			log2.renameTo(log3);
		}
		File log1 = new File(logFolder, "BotLog-1.log");
		if (log1.exists())
		{
			log1.renameTo(log2);
		}
		if (logFile.exists())
		{
			logFile.renameTo(log1);
		}
		FileUtils.initFile(logFile);
	}

	static void log(String s)
	{
		try
		{
			FileOutputStream log = new FileOutputStream(logFile, true);
			log.write((s + "\n").getBytes("UTF-8"));
			log.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	static boolean canLog()
	{
		return logFile.exists();
	}

}
