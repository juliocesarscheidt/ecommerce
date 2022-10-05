package com.github.juliocesarscheidt.ecommerce;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class NewOrderMain {
	
	private static Properties getProperties() {
		var properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9092");

	    // to send strings we need a string serializer
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		properties.put(ProducerConfig.ACKS_CONFIG, "all");
		properties.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
		properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
	    properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32 MB
	    properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 33554432); // 32 MB
	    properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // less compression than gzip, but faster
	    properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16 KB
	    
	    properties.put("delivery.timeout.ms", 60 * 1000); // 60 secs
	    properties.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5 ms
	    properties.put("request.timeout.ms", 30 * 1000); // 30 secs

		return properties;
	}
	
	public static void main(String[] args) {
		Properties properties = getProperties();
		properties.put("transactional.id", "1");

		var producer = new KafkaProducer<String, String>(properties);

		producer.initTransactions();

		try {
			producer.beginTransaction();

			String topic = "ECOMMERCE_NEW_ORDER";		

			for (int i = 0; i < 10; i ++) {
				String key = "key_" + i;
				String value = "value_" + i;

				var record = new ProducerRecord<String, String>(topic, key, value);
				System.out.println(record.toString());

				producer.send(record, (result, ex) -> {
					if (ex != null) {
						ex.printStackTrace();
						return;
					}

					System.out.println("Topic :: " + result.topic()
									  + " | Partition :: " + result.partition()
									  + " | Offset :: " + result.offset());
				}).get();
			}

			producer.commitTransaction();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			producer.abortTransaction();
		}

		producer.close();
	}
}
