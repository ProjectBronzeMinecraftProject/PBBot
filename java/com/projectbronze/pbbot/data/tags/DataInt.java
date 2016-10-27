package com.projectbronze.pbbot.data.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DataInt extends DataBase<Integer>
{

	public DataInt(Integer value)
	{
		super(value);
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(value);
	}
}
