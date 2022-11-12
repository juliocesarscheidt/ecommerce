package com.github.juliocesarscheidt.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocalDatabase {

	public final Connection connection;

	public LocalDatabase(String dbName) throws SQLException {
		this.connection = DriverManager.getConnection("jdbc:sqlite:target/" + dbName + ".db");
	}

	public void createTableIfNotExists(String statement) {
		try {
			this.create(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public PreparedStatement prepare(String statement, String ...params) throws SQLException {
		PreparedStatement preparedStatement = this.connection.prepareStatement(statement);
		for (int i = 0; i < params.length; i ++) {
			preparedStatement.setString(i + 1, params[i]);
		}
		return preparedStatement;
	}

	public void create(String statement) throws SQLException {
		this.connection.createStatement().execute(statement);
	}

	public ResultSet query(String statement, String ...params) throws SQLException {
		PreparedStatement preparedStatement = this.prepare(statement, params);
		return preparedStatement.executeQuery();
	}

	public void update(String statement, String ...params) throws SQLException {
		PreparedStatement preparedStatement = this.prepare(statement, params);
		preparedStatement.execute();
	}

	public void close() throws SQLException {
		connection.close();
	}
}
