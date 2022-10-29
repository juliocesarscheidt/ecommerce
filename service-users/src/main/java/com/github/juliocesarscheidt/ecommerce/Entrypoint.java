package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Entrypoint {

	public static void main(String[] args) {
		try {
			// create the sqlite file inside target/ folder
			String connectionString = "jdbc:sqlite:target/users_database.db";
			// open the connection
			Connection connection = DriverManager.getConnection(connectionString);
			String createTableSql = "CREATE TABLE IF NOT EXISTS Users (uuid varchar(255) primary key, email varchar(255))";
			connection.createStatement().execute(createTableSql);

			// create services injecting connection
			DispatchBatchMessageService dispatchBatchMessageService = new DispatchBatchMessageService(connection);
			CreateUserService createUserService = new CreateUserService(connection);

			// starting threads to consume from both topics
			new Thread() {
				@Override
			    public void run() {
				try (KafkaConsumerService<String> dispatchBatchMessageConsumerService = new KafkaConsumerService<>("DISPATCH_BATCH_MESSAGE",
																										dispatchBatchMessageService.getClass().getSimpleName(),
																										dispatchBatchMessageService::parse,
																										String.class)) {
						System.out.println("Consuming from DISPATCH_BATCH_MESSAGE");
						dispatchBatchMessageConsumerService.run();
					}
				}
			}.start();

			new Thread() {
				@Override
			    public void run() {
					try (KafkaConsumerService<Order> createUserConsumerService = new KafkaConsumerService<>("ECOMMERCE_NEW_ORDER",
																											createUserService.getClass().getSimpleName(),
																											createUserService::parse,
																											Order.class)) {
						
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
