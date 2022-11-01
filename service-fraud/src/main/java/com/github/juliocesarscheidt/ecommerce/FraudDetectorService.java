package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class FraudDetectorService {
	
	private final BigDecimal ORDER_AMOUNT_THRESHOLD = new BigDecimal("4000");

	private final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();

	public static void main(String[] args) {
		FraudDetectorService fraudService = new FraudDetectorService();		
		try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																			fraudService.getClass().getSimpleName(),
																			fraudService::parse)) {
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
		if (isFraud(order)) {
			System.out.println("Order REJECTED, it is a fraud attempt!");
			orderProducer.send("ECOMMERCE_ORDER_REJECTED", order.getEmail(), message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
		} else {
			System.out.println("Order APPROVED " + order);
			orderProducer.send("ECOMMERCE_ORDER_APPROVED", order.getEmail(), message.getId().continueWith(FraudDetectorService.class.getSimpleName()), order);
		}
	}

	private boolean isFraud(Order order) {
		// pretending that a fraud happens when the amount is >= ORDER_AMOUNT_THRESHOLD
		return order.getAmount().compareTo(ORDER_AMOUNT_THRESHOLD) >= 0;
	}
}
