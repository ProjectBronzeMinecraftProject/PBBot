package com.projectbronze.pbbot.data;

import java.io.File;
import com.projectbronze.pbbot.data.file.DataJson;
import com.projectbronze.pbbot.data.tags.DataBase;
import com.projectbronze.pbbot.data.tags.DataStorage;

public abstract class DataWrapperBase<T> implements IDataStorage
{
	public final T base;
	protected DataStorage meta;
	public DataWrapperBase(T base)
	{
		this.base = base;
		loadFromFile();
	}
	
	@Override
	public void setData(String key, DataBase data)
	{
		meta.setData(key, data);
		saveToFile();
	}

	@Override
	public DataBase getData(String key)
	{
		return meta.getData(key);
	}

	@Override
	public boolean hasData(String key)
	{
		return meta.hasData(key);
	}
	
	@Override
	public void removeData(String key)
	{
		meta.removeData(key);
		saveToFile();
	}
	
	protected void loadFromFile()
	{
		File file = getFile();
		if(file.exists())
		{
			meta = DataJson.load(file);
		}
		else
		{
			meta = new DataStorage();
		}
	}
	
	protected void saveToFile()
	{
		DataJson.write(getFile(), meta);
	}
	
	public abstract File getFile();
}
