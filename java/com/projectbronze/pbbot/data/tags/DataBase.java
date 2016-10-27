package com.projectbronze.pbbot.data.tags;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public abstract class DataBase<T>
{
	protected T value;

	public abstract JsonElement toJson();

	public static DataStorage fromJson(JsonObject o)
	{
		DataStorage ret = new DataStorage();
		o.entrySet().forEach(entry ->
		{
			ret.setData(entry.getKey(), fromJson(entry.getValue()));
		});
		return ret;
	}

	private static DataBase fromJson(JsonElement e)
	{
		if (e.isJsonObject())
		{
			return fromJson(e.getAsJsonObject());
		}
		if (e.isJsonPrimitive())
		{
			JsonPrimitive p = e.getAsJsonPrimitive();
			if (p.isBoolean())
			{
				return new DataBool(p.getAsBoolean());
			}
			if (p.isNumber())
			{
				float fval = p.getAsFloat();
				if (fval == (int) fval)
				{
					return new DataInt(p.getAsInt());
				}
				else
				{
					return new DataFloat(fval);
				}
			}
			if (p.isString())
			{
				return new DataString(p.getAsString());
			}

		}
		if (e.isJsonArray())
		{
			DataList list = new DataList();
			JsonArray a = e.getAsJsonArray();
			a.forEach(elem -> list.add(fromJson(elem)));
			return list;
		}
		if (e.isJsonNull())
		{
			return null;
		}
		return null;
	}

	public DataBase(T value)
	{
		this.value = value;
	}

	public T getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return toJson().toString();
	}
	
	@Override
	public int hashCode()
	{
		int result = 1;
		result = 37 * result + (value == null ? 0 : value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || obj.getClass() != getClass())
		{
			return false;
		}
		return value.equals(((DataBase) obj).value);
	}
	
}
