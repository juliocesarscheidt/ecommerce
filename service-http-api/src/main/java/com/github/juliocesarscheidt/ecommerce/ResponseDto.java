package com.github.juliocesarscheidt.ecommerce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ResponseDto {
	
	private String data;

	public ResponseDto(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(this, ResponseDto.class);
	}
}
