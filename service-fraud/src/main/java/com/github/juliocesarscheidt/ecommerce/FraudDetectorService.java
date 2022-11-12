package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class FraudDetectorService implements ConsumerService<Order> {
	
	private final BigDecimal ORDER_AMOUNT_THRESHOLD = new BigDecimal("4000");

	private final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();

	public static void main(String[] args) {
		// new ServiceProvider(FraudDetectorService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<Order>(FraudDetectorService::new).start(1);
	}

	/*
	FraudDetectorService fraudService = new FraudDetectorService();		
	try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																		fraudService.getClass().getSimpleName(),
																		fraudService::parse)) {
		service.run();
	}
	*/
	
	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}

	public String getConsumerGroup() {
		return FraudDetectorService.class.getSimpleName();
	}

	public void parse(ConsumerRecord<String, Message<Order>> record) {
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
