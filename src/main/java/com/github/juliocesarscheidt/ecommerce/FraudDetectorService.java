package com.github.juliocesarscheidt.ecommerce;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class FraudDetectorService {

	private static Properties getProperties() {
		var properties = new Properties();
		
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.0.3:9092");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
	    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
	    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
	    properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000);

	    return properties;
	}

	public static void main(String[] args) {
		Integer bufferBatchSize = 10; // number of messages to poll before commiting
		String topic = "ECOMMERCE_NEW_ORDER";	
	    List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
		Integer millisecondsToPoll = 1000; // 1 second

		Properties properties = getProperties();
	    String groupID = FraudDetectorService.class.getSimpleName();
	    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);	 

		var consumer = new KafkaConsumer<String, String>(properties);
		consumer.subscribe(Collections.singletonList(topic));

		while (true) {
			try {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(millisecondsToPoll));
				if (records.isEmpty()) {
					System.out.println("Any message found, continuing...");
					continue;
				}

				for (ConsumerRecord<String, String> record: records) {
			        buffer.add(record);
			        System.out.println("[INFO] record key " + record.key()
			        				  + " | record value " + record.value()
			        				  + " | record partition " + record.partition()
			        				  + " | record offset " + record.offset());
				}
	
				if (buffer.size() >= bufferBatchSize) {
					System.out.println("[INFO] committing messages");
			        // commit the offset
			        consumer.commitSync();
			        buffer.clear();
				}

				// sleep 1 secs
				Thread.sleep(1000); 
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				consumer.close();
				break;
			}
		}
	}
}
