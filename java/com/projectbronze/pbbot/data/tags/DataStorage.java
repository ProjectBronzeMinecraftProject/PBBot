package com.projectbronze.pbbot.data.tags;

import java.util.HashMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.projectbronze.pbbot.data.IDataStorage;

public class DataStorage extends DataBase<HashMap<String, DataBase>> implements IDataStorage
{

	public DataStorage()
	{
		super(new HashMap<String, DataBase>());
	}

	/**
	 * Always throw exception, use {@link #getData(String)}
	 */
	@Override
	@Deprecated
	public HashMap<String, DataBase> getValue()
	{
		throw new UnsupportedOperationException("Use getData");
	}
	
	@Override
	public void setData(String key, DataBase data)
	{
		value.put(key, data);
	}

	@Override
	public DataBase getData(String key)
	{
		return value.get(key);
	}

	@Override
	public JsonElement toJson()
	{
		JsonObject ret = new JsonObject();
		value.forEach((key, value) -> {
			ret.add(key, value.toJson());
		});
		return ret;
	}
	
	@Override
	public boolean hasData(String key)
	{
		return value.containsKey(key);
	}
	
	@Override
	public void removeData(String key)
	{
		value.remove(key);
	}
	
}
