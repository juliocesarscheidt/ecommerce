package com.github.juliocesarscheidt.ecommerce;

import java.io.File;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class ReadingReportService {

	private final File SOURCE = new File("src/main/resources/template_report.csv");

	public static void main(String[] args) {
		ReadingReportService readingReportService = new ReadingReportService();
		try (KafkaConsumerService<User> service = new KafkaConsumerService<>("ECOMMERCE_USER_GENERATE_READING_REPORT",
																			readingReportService.getClass().getSimpleName(),
																			readingReportService::parse)) {
			service.run();
		}
	}

	private void parse(ConsumerRecord<String, Message<User>> record) {
		System.out.println("[INFO] key " + record.key()
                      + " | value " + record.value()
                      + " | topic " + record.topic()
                      + " | partition " + record.partition()
                      + " | offset " + record.offset());

		var message = record.value();
		var user = (User) message.getPayload();
		var target = new File(user.getReportPath());
		IO.copyTo(SOURCE, target);

		var reportData = user.getReportData();
		IO.appendToFile(target, reportData);

		System.out.println("File created " + target.getAbsolutePath());
	}
}
