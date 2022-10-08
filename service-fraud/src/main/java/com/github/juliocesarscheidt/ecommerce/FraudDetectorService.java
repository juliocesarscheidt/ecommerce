package com.github.juliocesarscheidt.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class FraudDetectorService {

	private void parse(ConsumerRecord<String, Order> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
	}

	public static void main(String[] args) {
		FraudDetectorService fraudService = new FraudDetectorService();		
		try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																			fraudService.getClass().getSimpleName(),
																			fraudService::parse,
																			Order.class)) {
			service.run();
		}
	}
}
