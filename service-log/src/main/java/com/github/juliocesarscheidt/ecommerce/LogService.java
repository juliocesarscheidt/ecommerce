package com.github.juliocesarscheidt.ecommerce;

import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class LogService {

	private void parse(ConsumerRecord<String, Object> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
	}

	public static void main(String[] args) {
		LogService logService = new LogService();
		try (KafkaConsumerService<Object> service = new KafkaConsumerService<>(Pattern.compile("ECOMMERCE_.*"),
																			logService.getClass().getSimpleName(),
																			logService::parse,
																			Object.class)) {
			service.run();
		}
	}
}
