package com.github.juliocesarscheidt.ecommerce.consumer;

public interface ServiceFactory<T> {
	ConsumerService<T> create();
}
