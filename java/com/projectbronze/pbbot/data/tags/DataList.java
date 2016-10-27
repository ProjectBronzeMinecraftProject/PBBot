package com.projectbronze.pbbot.data.tags;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class DataList extends DataBase<List<DataBase>>
{
	public DataList()
	{
		super(new ArrayList<DataBase>());
	}
	
	public DataBase get(int idx)
	{
		return value.get(idx);
	}
	
	public void add(DataBase data)
	{
		value.add(data);
	}
	
	public void set(int idx, DataBase data)
	{
		value.set(idx, data);
	}
	
	public void remove(int idx)
	{
		value.remove(idx);
	}
	
	public void remove(DataBase data)
	{
		value.remove(data);
	}
	
	public boolean contains(DataBase data)
	{
		return value.contains(data);
	}
	
	public int size()
	{
		return value.size();
	}
	
	@Override
	public JsonElement toJson()
	{
		JsonArray ret = new JsonArray();
		value.forEach(data -> ret.add(data.toJson()));
		return ret;
	}
}
