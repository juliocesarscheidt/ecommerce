package com.github.juliocesarscheidt.ecommerce;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class DispatchBatchMessageService implements ConsumerService<String> {

	private static final KafkaProducerService<User> userProducer = new KafkaProducerService<>(false);
	private final LocalDatabase database;

	public DispatchBatchMessageService() throws SQLException {
		this.database = new LocalDatabase("users_database");
		this.database.createTableIfNotExists("CREATE TABLE IF NOT EXISTS Users (uuid varchar(255) primary key, email varchar(255))");
	}
	
	public static void main(String[] args) {
		// new ServiceProvider(DispatchBatchMessageService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<String>(DispatchBatchMessageService::new).start(1);
	}
	
	public String getTopic() {
		return "ECOMMERCE_DISPATCH_BATCH_MESSAGE";
	}

	public String getConsumerGroup() {
		return DispatchBatchMessageService.class.getSimpleName();
	}

	public void parse(ConsumerRecord<String, Message<String>> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());

		var message = record.value();
		var topic = message.getPayload();
		System.out.println("producing to topic " + topic);

		List<User> users = getUsers();
		for (User user : users) {
			System.out.println("user " + user);
			// send message asynchronous
			// concatenates already existing correlationId with a new information from our current class
			userProducer.sendAsync(topic, user.getEmail(), message.getId()
				.continueWith(DispatchBatchMessageService.class.getSimpleName()), user);
		}
	}

	private List<User> getUsers() {
		try {
			List<User> users = new ArrayList<>();
			String statement = "SELECT uuid, email FROM Users";
			ResultSet resultSet = this.database.query(statement);
			while (resultSet.next()) {
				users.add(new User(resultSet.getString("uuid"), resultSet.getString("email")));
			}
			return users;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
