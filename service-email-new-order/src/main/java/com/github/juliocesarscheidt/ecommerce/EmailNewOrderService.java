package com.github.juliocesarscheidt.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class EmailNewOrderService implements ConsumerService<Order> {
	
	private static final KafkaProducerService<Email> emailProducer = new KafkaProducerService<>();

	public static void main(String[] args) {
		// new ServiceProvider(EmailNewOrderService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<Order>(EmailNewOrderService::new).start(1);
	}

	/*
	EmailNewOrderService emailService = new EmailNewOrderService();		
	try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																		emailService.getClass().getSimpleName(),
																		emailService::parse)) {
		service.run();
	}
	*/

	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}

	public String getConsumerGroup() {
		return EmailNewOrderService.class.getSimpleName();
	}

	public void parse(ConsumerRecord<String, Message<Order>> record) {
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
