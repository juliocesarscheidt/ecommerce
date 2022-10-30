package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class CreateUserService {
	
	private final Connection connection;

	public CreateUserService(Connection connection) {
		this.connection = connection;
	}

	public void parse(ConsumerRecord<String, Message<Order>> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
		
		var message = record.value();
		var order = (Order) message.getPayload();
		String email = order.getEmail();
		if (isNewUser(email)) {
			insertNewUser(email);
		}
	}

	private void insertNewUser(String email) {
		String createUserSql = "INSERT INTO Users " +
				"(uuid, email) VALUES (?, ?)";
		try {
			String uuid = UUID.randomUUID().toString();
			
			PreparedStatement insert = this.connection.prepareStatement(createUserSql);
			insert.setString(1, uuid);
			insert.setString(2, email);
			insert.execute();
			System.out.println("Created new user with email " + email);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isNewUser(String email) {
		String selectUserSql = "SELECT uuid FROM Users " +
				"WHERE email = ? LIMIT 1";
		try {
			PreparedStatement select = this.connection.prepareStatement(selectUserSql);
			select.setString(1, email);
			ResultSet results = select.executeQuery();
			boolean exists = results.next();
			System.out.println("User with email " + email + " exists :: " + exists);
			return !exists;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
