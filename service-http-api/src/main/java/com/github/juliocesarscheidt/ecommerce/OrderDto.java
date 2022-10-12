package com.github.juliocesarscheidt.ecommerce;

import java.math.BigDecimal;

public class OrderDto {
	
	private final BigDecimal amount;
	private final String email;

	public OrderDto(BigDecimal amount, String email) {
		this.amount = amount;
		this.email = email;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "Order [amount=" + amount + ", email=" + email + "]";
	}
}
