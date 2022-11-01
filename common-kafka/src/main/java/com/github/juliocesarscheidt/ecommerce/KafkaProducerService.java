package com.github.juliocesarscheidt.ecommerce;

import java.io.Closeable;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerService<T> implements Closeable {

	private final KafkaProducer<String, Message<T>> producer;
	private final boolean transationsEnabled;

	KafkaProducerService() {
		// by default we are going to use transactions on kafka
		transationsEnabled = true;
		this.producer = new KafkaProducer<>(getProperties());
		if (this.transationsEnabled) this.producer.initTransactions();
	}
	
	KafkaProducerService(boolean transationsEnabled) {
		this.transationsEnabled = transationsEnabled;
		this.producer = new KafkaProducer<>(getProperties());
		if (this.transationsEnabled) this.producer.initTransactions();
	}

	void send(String topic, String key, CorrelationId correlationId, T payload) {
		try {
			if (this.transationsEnabled) this.producer.beginTransaction();

			// this will wait for the ack
			Future<RecordMetadata> future = sendAsync(topic, key, correlationId, payload);
			future.get();

			if (this.transationsEnabled) this.producer.commitTransaction();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			if (this.transationsEnabled) this.producer.abortTransaction();
		}
	}

	Future<RecordMetadata> sendAsync(String topic, String key, CorrelationId correlationId, T payload) {
		var message = new Message<>(correlationId, payload);

		var record = new ProducerRecord<>(topic, key, message);
		System.out.println(record.toString());

		// this will NOT wait
		return this.producer.send(record, callback());
	}

	private Callback callback() {
		return (result, ex) -> {
			if (ex != null) {
				ex.printStackTrace();
				return;
			}

			System.out.println("[KafkaProducerService] Topic :: " + result.topic()
							  + " | Partition :: " + result.partition()
							  + " | Offset :: " + result.offset());
		};
	}

	private Properties getProperties() {
		var properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9091,172.16.0.3:9092");

	    // to send strings we need a string serializer
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		// properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GsonSerializer.class.getName());

		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
	
		properties.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);

		// max number of unacked requests the client will send on a single request before blocking
		properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
	    properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 33554432); // 32 MB

	    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32 MB
	    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // less compression than gzip, but faster
	    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16 KB
	    
	    properties.put("delivery.timeout.ms", 60 * 1000); // 60 secs
	    properties.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5 ms
	    properties.put("request.timeout.ms", 30 * 1000); // 30 secs
	    
	    if (this.transationsEnabled) {
			String transactionId = UUID.randomUUID().toString();
			properties.put("transactional.id", transactionId);
	    }
	    
		return properties;
	}

	@Override
	public void close() {
		this.producer.close();
	}
}
