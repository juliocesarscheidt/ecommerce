package com.github.juliocesarscheidt.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.KafkaConsumerService;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class EmailNewOrderService {
	
	private static final KafkaProducerService<Email> emailProducer = new KafkaProducerService<>();

	public static void main(String[] args) {
		EmailNewOrderService emailService = new EmailNewOrderService();		
		try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																			emailService.getClass().getSimpleName(),
																			emailService::parse)) {
			service.run();
		}
	}

	private void parse(ConsumerRecord<String, Message<Order>> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());

		var message = record.value();
		var order = (Order) message.getPayload();

		var userEmail = order.getEmail();
		var id = message.getId().continueWith(EmailNewOrderService.class.getSimpleName());

		Email emailContent = new Email(userEmail, "<h1>Thank you for your order " + userEmail + "! We are processing your request</h1>");
		emailProducer.send("ECOMMERCE_SEND_EMAIL", userEmail, id, emailContent);
	}
}
