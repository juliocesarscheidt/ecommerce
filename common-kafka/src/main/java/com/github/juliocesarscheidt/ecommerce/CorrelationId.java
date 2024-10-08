package com.github.juliocesarscheidt.ecommerce;

import java.util.UUID;

public class CorrelationId {

	private final String id;

	public CorrelationId(String title) {
		id = title + "(" + UUID.randomUUID().toString() + ")";
	}

	public CorrelationId continueWith(String title) {
		return new CorrelationId(id + "-" + title);
	}

	@Override
	public String toString() {
		return "CorrelationId [id=" + id + "]";
	}
}
