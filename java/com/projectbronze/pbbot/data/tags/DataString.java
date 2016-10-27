package com.projectbronze.pbbot.data.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DataString extends DataBase<String>
{

	public DataString(String value)
	{
		super(value);
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(value);
	}
}
