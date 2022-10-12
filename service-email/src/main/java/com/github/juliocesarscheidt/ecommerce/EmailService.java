package com.github.juliocesarscheidt.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class EmailService {

	private void parse(ConsumerRecord<String, Email> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
		
		Email emailContent = record.value();
		String email = emailContent.getSubject();
		String emailBody = emailContent.getBody();

		System.out.println("Pretending we would send an email to the user");
	}

	public static void main(String[] args) {
		EmailService emailService = new EmailService();		
		try (KafkaConsumerService<Email> service = new KafkaConsumerService<>("ECOMMERCE_SEND_EMAIL",
																			emailService.getClass().getSimpleName(),
																			emailService::parse,
																			Email.class)) {
			service.run();
		}
	}
}
