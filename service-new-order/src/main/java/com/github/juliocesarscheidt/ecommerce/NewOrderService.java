package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;

public class NewOrderService {

	private static final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();
	private static final KafkaProducerService<Email> emailProducer = new KafkaProducerService<>();

	public static void main(String[] args) {

		try {
			String userId = UUID.randomUUID().toString();
			String orderId = UUID.randomUUID().toString();
			BigDecimal orderAmount = new BigDecimal(Math.random() * 5000 + 1);

			Order order = new Order(userId, orderId, orderAmount);
			orderProducer.send("ECOMMERCE_NEW_ORDER", userId, order);

			Email email = new Email(userId, "Thank you for your order " + userId + "! We are processing your request");
			emailProducer.send("ECOMMERCE_SEND_EMAIL", userId, email);

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			orderProducer.close();
			emailProducer.close();
		}
	}
}
