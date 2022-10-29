package com.github.juliocesarscheidt.ecommerce;

import java.time.LocalDateTime;

public class User {
	
	private final String uuid;
	private final String email;

	public User(String uuid, String email) {
		this.uuid = uuid;
		this.email = email;
	}
	
	public String getUuid() {
		return uuid;
	}

	public String getEmail() {
		return email;
	}

	public String getReportPath() {
		return "target/" + uuid + "_report.csv";
	}
	
	public String getReportData() {
		return uuid + ";" + email + ";" + LocalDateTime.now();
	}

	@Override
	public String toString() {
		return "User [uuid=" + uuid + ", email=" + email + "]";
	}
}
