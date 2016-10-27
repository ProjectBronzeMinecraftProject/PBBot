package com.projectbronze.pbbot.data;

import com.projectbronze.pbbot.data.tags.DataBase;
import com.projectbronze.pbbot.data.tags.DataBool;
import com.projectbronze.pbbot.data.tags.DataFloat;
import com.projectbronze.pbbot.data.tags.DataInt;
import com.projectbronze.pbbot.data.tags.DataList;
import com.projectbronze.pbbot.data.tags.DataStorage;
import com.projectbronze.pbbot.data.tags.DataString;

public interface IDataStorage
{
	public void setData(String key, DataBase data);
	public DataBase getData(String key);
	public boolean hasData(String key);
	public void removeData(String key);
	
	public default void setInt(String key, int data)
	{
		setData(key, new DataInt(data));
	}
	
	public default int getInt(String key, int def)
	{
		DataInt data = (DataInt) getData(key);
		return data == null ? def : data.getValue();
	}
	
	public default int getInt(String key)
	{
		return getInt(key, 0);
	}
	
	public default void setFloat(String key, float data)
	{
		setData(key, new DataFloat(data));
	}
	
	public default float getFloat(String key, float def)
	{
		DataFloat data = (DataFloat) getData(key);
		return data == null ? def : data.getValue();
	}
	
	public default float getFloat(String key)
	{
		return getFloat(key, 0.0F);
	}
	
	public default void setBool(String key, boolean data)
	{
		setData(key, new DataBool(data));
	}
	
	public default boolean getBool(String key, boolean def)
	{
		DataBool data = (DataBool) getData(key);
		return data == null ? def : data.getValue();
	}
	
	public default boolean getBoot(String key)
	{
		return getBool(key, false);
	}
	
	public default void setString(String key, String data)
	{
		setData(key, new DataString(data));
	}
	
	public default String getString(String key)
	{
		DataString data = (DataString) getData(key);
		return data == null ? null : data.getValue();
	}
	
	public default DataStorage getTag(String key)
	{
		return (DataStorage) getData(key);
	}
	
	public default DataList getList(String key)
	{
		return (DataList) getData(key);
	}
	
}
