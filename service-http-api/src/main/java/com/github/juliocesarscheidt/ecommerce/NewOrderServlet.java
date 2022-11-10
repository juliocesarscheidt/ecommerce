package com.github.juliocesarscheidt.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NewOrderServlet extends HttpServlet {
	
	private static final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();
	// private static final KafkaProducerService<Email> emailProducer = new KafkaProducerService<>();

	private final Gson gson = new GsonBuilder().create();

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		orderProducer.close();
		// emailProducer.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String bodyRaw = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		System.out.println(bodyRaw);

		try {
			OrderDto orderDto = gson.fromJson(bodyRaw, OrderDto.class);
			System.out.println(orderDto);

			String userEmail = orderDto.getEmail();
			BigDecimal orderAmount = orderDto.getAmount();
			String orderId = UUID.randomUUID().toString();
	
			Order order = new Order(orderId, orderAmount, userEmail);
			orderProducer.send("ECOMMERCE_NEW_ORDER", userEmail, new CorrelationId(NewOrderServlet.class.getSimpleName()), order);

			// Email emailContent = new Email(userEmail, "<h1>Thank you for your order " + userEmail + "! We are processing your request</h1>");
			// emailProducer.send("ECOMMERCE_SEND_EMAIL", userEmail, new CorrelationId(NewOrderServlet.class.getSimpleName()), emailContent);

			ResponseDto res = new ResponseDto("Order is being processed");
			response.setCharacterEncoding("utf-8");
	        response.setContentType("application/json");
	        response.setStatus(HttpServletResponse.SC_ACCEPTED);
	        response.getWriter().println(res);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
