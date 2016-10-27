package com.projectbronze.pbbot.data.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.projectbronze.pbbot.Core;
import com.projectbronze.pbbot.data.tags.DataBase;
import com.projectbronze.pbbot.data.tags.DataStorage;
import com.projectbronze.pbbot.utils.FileUtils;

public class DataJson
{
	public static void write(File f, DataBase base)
	{
		try(BufferedWriter w = FileUtils.createWriter(f, false);)
		{
			w.write(new GsonBuilder().setPrettyPrinting().create().toJson(base.toJson()));
		}
		catch (IOException e)
		{
			Core.err.error("Unable to write data file " + f.getName());
			e.printStackTrace(Core.err);
		}
	}
	
	public static DataStorage load(File f)
	{
		try(FileReader r= new FileReader(f))
		{
			return DataBase.fromJson(new JsonParser().parse(r).getAsJsonObject());
		}
		catch (Exception e)
		{
			Core.err.error("Unable to load data file " + f.getName());
			e.printStackTrace(Core.err);
			return null;
		}
	}
}
