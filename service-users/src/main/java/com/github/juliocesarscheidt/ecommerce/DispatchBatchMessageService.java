package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class DispatchBatchMessageService {

	private final Connection connection;

	private static final KafkaProducerService<User> userProducer = new KafkaProducerService<>(false);

	public DispatchBatchMessageService(Connection connection) {
		this.connection = connection;
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
		String getUsersSql = "SELECT uuid, email FROM Users";
		try {
			List<User> users = new ArrayList<>();
			PreparedStatement select = this.connection.prepareStatement(getUsersSql);
			ResultSet resultSet = select.executeQuery();
			while (resultSet.next()) {
				User user = new User(resultSet.getString("uuid"), resultSet.getString("email"));
				users.add(user);
			}
			return users;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
