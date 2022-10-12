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

public class KafkaConsumerService<T> implements Closeable {

	private final KafkaConsumer<String, T> consumer;
	private final ConsumerFunction<T> parse;

	private KafkaConsumerService(String consumerName, ConsumerFunction<T> parse, Class<T> type) {
		this.consumer = new KafkaConsumer<>(getProperties(consumerName, type));
		this.parse = parse;
	}

	public KafkaConsumerService(String topic, String consumerName, ConsumerFunction<T> parse, Class<T> type) {
		this(consumerName, parse, type);
		this.consumer.subscribe(Collections.singletonList(topic));
	}

	public KafkaConsumerService(Pattern compile, String consumerName, ConsumerFunction<T> parse, Class<T> type) {
		this(consumerName, parse, type);
		this.consumer.subscribe(compile);
	}

	void run() {
		// Integer bufferBatchSize = 1; // number of messages to poll before commiting
	    // List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
		Integer millisecondsToPoll = 1000; // 1 second

		while (true) {
			try {
				ConsumerRecords<String, T> records = this.consumer.poll(Duration.ofMillis(millisecondsToPoll));
				if (records.isEmpty()) {
					System.out.println("Any message found, continuing...");
					Thread.sleep(1000); // sleep 1 secs
					continue;
				}

				for (ConsumerRecord<String, T> record: records) {
			        // buffer.add(record);
					try {						
						this.parse.consume(record);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
	
	private Properties getProperties(String consumerName, Class<T> type) {
		var properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9091,172.16.0.3:9092");

		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		// properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());

		properties.put(GsonDeserializer.TYPE_CONFIG, type.getName());

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
