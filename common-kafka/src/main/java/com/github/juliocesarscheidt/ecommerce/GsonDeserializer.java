package com.github.juliocesarscheidt.ecommerce;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonDeserializer<T> implements Deserializer<T> {

	public static final Object TYPE_CONFIG = "com.github.juliocesarscheidt.ecommerce.type_config";
	
	private final Gson gson = new GsonBuilder().create();
	private Class<T> type;

	@Override
	@SuppressWarnings("unchecked")
	public void configure(Map<String, ?> configs, boolean isKey) {
		String typeName = String.valueOf(configs.get(TYPE_CONFIG));
		try {
			this.type = (Class<T>) Class.forName(typeName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("The type for deserialization does not exist in the classpath", e);
			
		}
	
		Deserializer.super.configure(configs, isKey);
	}
	
	@Override
	public T deserialize(String s, byte[] bytes) {
		return gson.fromJson(new String(bytes), type);
	}
}
