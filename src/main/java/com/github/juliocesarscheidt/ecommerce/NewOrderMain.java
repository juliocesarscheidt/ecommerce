package com.github.juliocesarscheidt.ecommerce;

import java.util.UUID;

public class NewOrderMain {

	public static void main(String[] args) {
		try (KafkaProducerService producer = new KafkaProducerService()) {
			String topicOrder = "ECOMMERCE_NEW_ORDER";
			String keyOrder = UUID.randomUUID().toString();
			String messageOrder = "Hello World";
			producer.send(topicOrder, keyOrder, messageOrder);

			String topicEmail = "ECOMMERCE_SEND_EMAIL";
			String keyEmail = UUID.randomUUID().toString();
			String messageEmail = "Thank you for your order! We are processing your request";
			producer.send(topicEmail, keyEmail, messageEmail);

			producer.close();
		}
	}
}
