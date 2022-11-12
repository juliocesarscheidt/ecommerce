package com.github.juliocesarscheidt.ecommerce.consumer;

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

import com.github.juliocesarscheidt.ecommerce.Message;
import com.github.juliocesarscheidt.ecommerce.producer.GsonSerializer;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class KafkaConsumerService<T> implements Closeable {

	private final KafkaConsumer<String, Message<T>> consumer;
	private final ConsumerFunction<T> parser;
	private static final KafkaProducerService<Object> deadLetterProducer = new KafkaProducerService<>(false);

	public KafkaConsumerService(String consumerName, ConsumerFunction<T> parser) {
		this.consumer = new KafkaConsumer<>(getProperties(consumerName));
		this.parser = parser;
	}

	public KafkaConsumerService(String topic, String consumerName, ConsumerFunction<T> parse) {
		this(consumerName, parse);
		this.consumer.subscribe(Collections.singletonList(topic));
	}

	public KafkaConsumerService(Pattern compile, String consumerName, ConsumerFunction<T> parse) {
		this(consumerName, parse);
		this.consumer.subscribe(compile);
	}

	public void run() {
		// Integer bufferBatchSize = 100; // number of messages to poll before commiting
	    // List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
		Integer millisecondsToPoll = 100; // 100 ms

		while (true) {
			try {
				ConsumerRecords<String, Message<T>> records = this.consumer.poll(Duration.ofMillis(millisecondsToPoll));
				if (records.isEmpty()) {
					// System.out.println("[KakfaConsumerService] Any message found, continuing...");
					Thread.sleep(100); // sleep 100 ms
					continue;
				}

				System.out.println("[KakfaConsumerService] Found " + records.count() + " records!");

				for (ConsumerRecord<String, Message<T>> record: records) {
			        // buffer.add(record);
					try {						
						this.parser.consume(record);

					} catch (Exception e) {
						e.printStackTrace();

						// send to dead letter
						var message = record.value();
						try (GsonSerializer<Object> gson = new GsonSerializer<>()) {
							deadLetterProducer.send("ECOMMERCE_DEADLETTER", message.getId().toString(), message.getId().continueWith("DeadLetter"), gson.serialize(message));
						}
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
	
	private Properties getProperties(String consumerName) {
		var properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9091,172.16.0.3:9092");

		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		// properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());

	    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // earliest latest

	    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // true false

	    properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000"); // default 5000

	    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");

	    // properties.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // read_uncommitted read_committed

	    properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerName);	 
	    properties.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerName + "-" + UUID.randomUUID().toString()); 

	    return properties;
	}

	@Override
	public void close() {
		this.consumer.close();
	}
}
