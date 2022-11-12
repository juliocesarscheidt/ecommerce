package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;

public class OrderDto {
	
	private final String uuid;
	private final BigDecimal amount;
	private final String email;

	public OrderDto(String uuid, BigDecimal amount, String email) {
		this.uuid = uuid;
		this.amount = amount;
		this.email = email;
	}

	public String getUuid() {
		return uuid;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "OrderDto [uuid=" + uuid + ", amount=" + amount + ", email=" + email + "]";
	}
}
