package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.github.juliocesarscheidt.ecommerce.consumer.ConsumerService;
import com.github.juliocesarscheidt.ecommerce.consumer.ServiceRunner;
import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

public class FraudDetectorService implements ConsumerService<Order> {
	
	private final BigDecimal ORDER_AMOUNT_THRESHOLD = new BigDecimal("4000");
	private final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();
	private final LocalDatabase database;

	public FraudDetectorService() throws SQLException {
		this.database = new LocalDatabase("frauds_database");
		this.database.createTableIfNotExists("CREATE TABLE IF NOT EXISTS Orders (uuid varchar(255) primary key, is_fraud varchar(255))");
	}
	
	public static void main(String[] args) {
		// new ServiceProvider(FraudDetectorService::new).call();
		// service runner will create the provider with a factory and call this provider X times
		new ServiceRunner<Order>(FraudDetectorService::new).start(1);
	}

	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}

	public String getConsumerGroup() {
		return FraudDetectorService.class.getSimpleName();
	}

	public void parse(ConsumerRecord<String, Message<Order>> record) {
		System.out.println("[INFO] key " + record.key()
						  + " | value " + record.value()
						  + " | topic " + record.topic()
						  + " | partition " + record.partition()
						  + " | offset " + record.offset());

		var message = record.value();
		var order = (Order) message.getPayload();

		if (isProcessed(order)) {
			System.out.println("Order " + order.getOrderId() + " is already processed!");
			return;
		}

		try {
			var isFraud = isFraud(order);
			var messageId = message.getId().continueWith(FraudDetectorService.class.getSimpleName());
			if (isFraud) {
				System.out.println("Order REJECTED, it is a fraud attempt!");
				orderProducer.send("ECOMMERCE_ORDER_REJECTED", order.getEmail(), messageId, order);
			} else {
				System.out.println("Order APPROVED " + order);
				orderProducer.send("ECOMMERCE_ORDER_APPROVED", order.getEmail(), messageId, order);
			}
			this.database.update("INSERT INTO Orders (uuid, is_fraud) VALUES (?, ?)", order.getOrderId(), isFraud ? "true" : "false");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isProcessed(Order order) {
		try {
			ResultSet resultSet = this.database.query("SELECT uuid FROM Orders WHERE uuid = ? LIMIT 1", order.getOrderId());
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isFraud(Order order) {
		// pretending that a fraud happens when the amount is >= ORDER_AMOUNT_THRESHOLD
		return order.getAmount().compareTo(ORDER_AMOUNT_THRESHOLD) >= 0;
	}
}
