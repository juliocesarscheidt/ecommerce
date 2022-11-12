package com.github.juliocesarscheidt.ecommerce.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.Message;

public interface ConsumerService<T> {
	void parse(ConsumerRecord<String, Message<T>> record);
	String getTopic();
	String getConsumerGroup();
}
