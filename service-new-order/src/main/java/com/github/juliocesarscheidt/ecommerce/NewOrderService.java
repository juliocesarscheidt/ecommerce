package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;
import java.util.UUID;

import com.github.javafaker.Faker;

public class NewOrderService {

	private static final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();
	private static final KafkaProducerService<Email> emailProducer = new KafkaProducerService<>();

	public static void main(String[] args) {
		Faker faker = new Faker();
		Integer numberOfOrders = 10;
		if (args.length > 0 && !args[0].equals(null)) {
			numberOfOrders = Integer.parseInt(args[0]);
		}

		try {
			// 10 orders for same user
			String userEmail = faker.bothify("??????##@gmail.com");

			for (int i = 0; i < numberOfOrders; i ++) {
				BigDecimal orderAmount = new BigDecimal(Math.random() * 5000 + 1);
				String orderId = UUID.randomUUID().toString();

				Order order = new Order(orderId, orderAmount, userEmail);
				orderProducer.send("ECOMMERCE_NEW_ORDER", userEmail, new CorrelationId(NewOrderService.class.getSimpleName()), order);

				Email emailContent = new Email(userEmail, "<h1>Thank you for your order " + userEmail + "! We are processing your request</h1>");
				emailProducer.send("ECOMMERCE_SEND_EMAIL", userEmail, new CorrelationId(NewOrderService.class.getSimpleName()), emailContent);
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			orderProducer.close();
			emailProducer.close();
		}
	}
}
