package com.projectbronze.pbbot.data.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DataFloat extends DataBase<Float>
{

	public DataFloat(Float value)
	{
		super(value);
	}

	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(value);
	}

}
