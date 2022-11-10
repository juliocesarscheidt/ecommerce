package com.github.juliocesarscheidt.ecommerce.consumer;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.Message;

public interface ConsumerFunction<T> {
	void consume(ConsumerRecord<String, Message<T>> record) throws ExecutionException;
}
