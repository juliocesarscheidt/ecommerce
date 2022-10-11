package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class CreateUserService {
	
	private Connection connection;

	CreateUserService() {
		// create the sqlite file inside target/ folder
		String connectionString = "jdbc:sqlite:target/users_database.db";
		try {
			this.connection = DriverManager.getConnection(connectionString);
			String createTableSql = "CREATE TABLE IF NOT EXISTS Users " +
					"(uuid varchar(255) primary key, email varchar(255))";
			this.connection.createStatement().execute(createTableSql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CreateUserService createUserService = new CreateUserService();		
		try (KafkaConsumerService<Order> service = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																			createUserService.getClass().getSimpleName(),
																			createUserService::parse,
																			Order.class)) {
			service.run();
		}
	}

	private void parse(ConsumerRecord<String, Order> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());
		
		var order = (Order) record.value();
		String email = order.getEmail();
		if (isNewUser(email)) {
			insertNewUser(email);
		}
		
	}

	private void insertNewUser(String email) {
		String createUserSql = "INSERT INTO Users " +
				"(uuid, email) VALUES (?, ?)";
		try {
			PreparedStatement insert = this.connection.prepareStatement(createUserSql);
			insert.setString(1, "uuid");
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
			System.out.println("exists " + exists);
			return !exists;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
