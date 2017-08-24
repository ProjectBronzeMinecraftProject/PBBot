package com.gt22.pbbot.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jooq.lambda.Unchecked;

import java.io.FileReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

//===============================================================================================================
//				Я серьёзно советую не читать код который идёт дальше если он ещё работает
//				Чтение этого кода может закончится психической травмой от переизбытка рефлексии
//===============================================================================================================
public class ConfigUtils {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ManualConfigProperty {}

	public static <T> T loadConfig(Class<T> configClass, String configFilename, JsonElement defaultConfig) throws Exception {
		return loadConfig(configClass, new JsonParser().parse(new FileReader(configFilename)).getAsJsonObject(), defaultConfig.getAsJsonObject());
	}

	public static <T> T loadConfig(Class<T> configClass, JsonObject config, JsonObject defaultConfig) throws Exception {
		T cfg = configClass.newInstance();
		Arrays.stream(configClass.getFields()).forEach(Unchecked.consumer(f -> {
			if(!f.isAnnotationPresent(ManualConfigProperty.class)) {
				String name = f.getName();
				f.setAccessible(true);
				JsonElement value = config.has(name) ? config.get(name) : defaultConfig.get(name);
				if (value == null) {
					f.set(cfg, null);
				} else {
					f.set(cfg, convertElementToType(f.getType(), value));
				}
			}
		}));
		try {
			Method loadManual = configClass.getMethod("loadManual", JsonObject.class);
			loadManual.invoke(cfg, config);
		} catch(NoSuchMethodException e) {
			//No manual load
		}
		return cfg;
	}

	private static Object convertElementToType(Class<?> type, JsonElement e) {
		if (type == String.class) {
			return e.getAsString();
		} else if (type == boolean.class || type == Boolean.class) {
			return e.getAsBoolean();
		} else if (type == byte.class || type == Byte.class) {
			return e.getAsByte();
		} else if (type == short.class || type == Short.class) {
			return e.getAsShort();
		} else if (type == int.class || type == Integer.class) {
			return e.getAsInt();
		} else if (type == long.class || type == Long.class) {
			return e.getAsLong();
		} else if (type == double.class || type == Double.class) {
			return e.getAsDouble();
		} else if (type == float.class || type == Float.class) {
			return e.getAsFloat();
		} else if (type == char.class || type == Character.class) {
			return e.getAsCharacter();
		} else if (type == Number.class) {
			return e.getAsNumber();
		} else if (type == BigInteger.class) {
			return e.getAsBigInteger();
		} else if (type == BigDecimal.class) {
			return e.getAsBigDecimal();
		} else if (type.isArray()) {
			JsonArray arr = e.getAsJsonArray();
			Class<?> arrType = type.getComponentType();
			Object ret = Array.newInstance(arrType, arr.size());

			for(int i = 0; i < arr.size(); i++) {
				Object o = convertElementToType(arrType, arr.get(i));
				Array.set(ret, i, o);
			}
			return ret;
		} else {
			throw new IllegalArgumentException("Invalid type " + type.getName());
		}
	}

}
