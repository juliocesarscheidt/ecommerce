package com.github.juliocesarscheidt.ecommerce;

import java.io.File;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;

public class ReadingReportService implements ConsumerService<User> {

	private final File SOURCE = new File("src/main/resources/template_report.csv");

	public static void main(String[] args) {
		// new ServiceProvider(ReadingReportService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<User>(ReadingReportService::new).start(2);
	}

	public String getTopic() {
		return "ECOMMERCE_USER_GENERATE_READING_REPORT";
	}

	public String getConsumerGroup() {
		return ReadingReportService.class.getSimpleName();
	}

	public void parse(ConsumerRecord<String, Message<User>> record) {
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
