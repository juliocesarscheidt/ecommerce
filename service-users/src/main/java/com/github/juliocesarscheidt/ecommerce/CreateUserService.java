package com.github.juliocesarscheidt.ecommerce;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;

public class CreateUserService implements ConsumerService<Order> {
	
	private final LocalDatabase database;

	public CreateUserService() throws SQLException {
		this.database = new LocalDatabase("users_database");
		this.database.createTableIfNotExists("CREATE TABLE IF NOT EXISTS Users (uuid varchar(255) primary key, email varchar(255))");
	}

	public static void main(String[] args) {
		// new ServiceProvider(CreateUserService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<Order>(CreateUserService::new).start(1);
	}
	
	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}

	public String getConsumerGroup() {
		return CreateUserService.class.getSimpleName();
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
		try {
			String uuid = UUID.randomUUID().toString();
			this.database.update("INSERT INTO Users (uuid, email) VALUES (?, ?)", uuid, email);
			System.out.println("Created new user with uuid " + uuid + " and email " + email);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isNewUser(String email) {
		try {
			ResultSet results = this.database.query("SELECT uuid FROM Users WHERE email = ? LIMIT 1", email);
			boolean exists = results.next();
			System.out.println("User with email " + email + " exists :: " + exists);
			return !exists;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
