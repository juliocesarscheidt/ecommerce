package com.github.juliocesarscheidt.ecommerce.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import com.github.juliocesarscheidt.ecommerce.Message;
import com.github.juliocesarscheidt.ecommerce.MessageAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonDeserializer implements Deserializer<Message> {

	// public static final Object TYPE_CONFIG = "com.github.juliocesarscheidt.ecommerce.type_config";

	private final Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageAdapter()).create();

	@Override
	public Message deserialize(String s, byte[] bytes) {
		// return gson.fromJson(new String(bytes), type);
		return gson.fromJson(new String(bytes), Message.class);
	}

	public Message deserialize(byte[] bytes) {
		return gson.fromJson(new String(bytes), Message.class);
	}

	/*
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
	*/
}
