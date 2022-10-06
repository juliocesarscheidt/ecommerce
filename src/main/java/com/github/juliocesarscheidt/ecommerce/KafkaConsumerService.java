package com.github.juliocesarscheidt.ecommerce;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class KafkaConsumerService implements Closeable {

	private final KafkaConsumer<String, String> consumer;
	private final ConsumerFunction parse;

	KafkaConsumerService(String topic, String consumerName, ConsumerFunction parse) {
		this.consumer = new KafkaConsumer<>(properties(consumerName));
		this.consumer.subscribe(Collections.singletonList(topic));
		this.parse = parse;
	}

	public KafkaConsumerService(Pattern compile, String consumerName, ConsumerFunction parse) {
		this.consumer = new KafkaConsumer<String, String>(properties(consumerName));
		this.consumer.subscribe(compile);
		this.parse = parse;
	}

	void run() {
		// Integer bufferBatchSize = 1; // number of messages to poll before commiting
	    // List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
		Integer millisecondsToPoll = 1000; // 1 second

		while (true) {
			try {
				ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(millisecondsToPoll));
				if (records.isEmpty()) {
					System.out.println("Any message found, continuing...");
					Thread.sleep(1000); // sleep 1 secs
					continue;
				}

				for (ConsumerRecord<String, String> record: records) {
			        // buffer.add(record);
					this.parse.consume(record);
				}

//				if (buffer.size() >= bufferBatchSize) {
//					System.out.println("[INFO] committing messages");
//			        consumer.commitSync(); // commit the offset
//			        buffer.clear();
//				}

			} catch (InterruptedException e) {
				e.printStackTrace();
				consumer.close();
				break;
			}
		}
	}
	
	private Properties properties(String consumerName) {
		var properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9092");

		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

	    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

	    // properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
	    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

	    // properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
	    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);

	    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerName);	 
	    properties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerName + "-" + UUID.randomUUID().toString()); 

	    return properties;
	}

	@Override
	public void close() {
		this.consumer.close();
	}
}
