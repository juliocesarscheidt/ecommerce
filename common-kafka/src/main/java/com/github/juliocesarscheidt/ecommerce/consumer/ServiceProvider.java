package com.github.juliocesarscheidt.ecommerce.consumer;

import java.util.concurrent.Callable;

public class ServiceProvider<T> implements Callable<Void> {

	private final ServiceFactory<T> factory;

	public ServiceProvider(ServiceFactory<T> factory) {
		this.factory = factory;
	}

	public Void call() throws Exception {
		ConsumerService<T> consumerService = factory.create();
		try (KafkaConsumerService<T> service = new KafkaConsumerService<>(consumerService.getTopic(),
																		consumerService.getConsumerGroup(),
																		consumerService::parse)) {
			service.run();
		}
		return null;
	}
}
