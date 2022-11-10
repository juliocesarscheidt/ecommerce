package com.github.juliocesarscheidt.ecommerce.producer;

import org.apache.kafka.common.serialization.Serializer;

import com.github.juliocesarscheidt.ecommerce.Message;
import com.github.juliocesarscheidt.ecommerce.MessageAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSerializer<T> implements Serializer<T> {

	private final Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageAdapter()).create();

	@Override
	public byte[] serialize(String s, T object) {
		return gson.toJson(object).getBytes();
	}

	public byte[] serialize(T object) {
		return gson.toJson(object).getBytes();
	}
}
