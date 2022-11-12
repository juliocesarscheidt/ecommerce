package com.github.juliocesarscheidt.ecommerce;

import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrdersDatabase implements Closeable {
	
	private final LocalDatabase database;

	public OrdersDatabase() throws SQLException {
		this.database = new LocalDatabase("orders_database");
		this.database.createTableIfNotExists("CREATE TABLE IF NOT EXISTS Orders (uuid varchar(255) primary key, amount decimal(10, 4), email varchar(255))");
	}

	public boolean saveNew(Order order) {
		if (isProcessed(order)) {
			return false;
		}
		try {
			this.database.update("INSERT INTO Orders (uuid, amount, email) VALUES (?, ?, ?)", order.getOrderId(), order.getAmount().toString(), order.getEmail());
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
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

	@Override
	public void close() throws IOException {
		try {
			this.database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
