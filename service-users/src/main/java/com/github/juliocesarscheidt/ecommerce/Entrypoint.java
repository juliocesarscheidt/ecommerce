package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.github.juliocesarscheidt.ecommerce.consumer.KafkaConsumerService;

public class Entrypoint {
	
	private static void setupDatabase(Connection connection) {
		try {
			String createTableSql = "CREATE TABLE IF NOT EXISTS Users (uuid varchar(255) primary key, email varchar(255))";
			connection.createStatement().execute(createTableSql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			// create the sqlite file inside target/ folder
			String connectionString = "jdbc:sqlite:target/users_database.db";
			// open the connection
			Connection connection = DriverManager.getConnection(connectionString);
			setupDatabase(connection);

			// create services injecting connection
			DispatchBatchMessageService dispatchBatchMessageService = new DispatchBatchMessageService(connection);
			CreateUserService createUserService = new CreateUserService(connection);

			// starting threads to consume from both topics
			new Thread() {
				@Override
				public void run() {
				try (KafkaConsumerService<String> dispatchBatchMessageConsumerService = new KafkaConsumerService<>("ECOMMERCE_DISPATCH_BATCH_MESSAGE",
																											dispatchBatchMessageService.getClass().getSimpleName(),
																											dispatchBatchMessageService::parse)) {
						System.out.println("Consuming from ECOMMERCE_DISPATCH_BATCH_MESSAGE");
						dispatchBatchMessageConsumerService.run();
					}
				}
			}.start();

			new Thread() {
				@Override
				public void run() {
					try (KafkaConsumerService<Order> createUserConsumerService = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																											createUserService.getClass().getSimpleName(),
																											createUserService::parse)) {

						System.out.println("Consuming from ECOMMERCE_NEW_ORDER");
						createUserConsumerService.run();
					}
				}
			}.start();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
