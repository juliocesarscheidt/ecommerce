package com.github.juliocesarscheidt.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NewOrderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final Gson gson = new GsonBuilder().create();
	private static final KafkaProducerService<Order> orderProducer = new KafkaProducerService<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		orderProducer.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String bodyRaw = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		System.out.println(bodyRaw);

		try {			
			OrderDto orderDto = gson.fromJson(bodyRaw, OrderDto.class);
			System.out.println(orderDto);

			String orderId = orderDto.getUuid();
			String userEmail = orderDto.getEmail();
			BigDecimal orderAmount = orderDto.getAmount();

			Order order = new Order(orderId, orderAmount, userEmail);
			
			response.setCharacterEncoding("utf-8");
	        response.setContentType("application/json");
			ResponseDto res;

			try (var database = new OrdersDatabase()) {
				if (database.saveNew(order)) {
					orderProducer.send("ECOMMERCE_NEW_ORDER", userEmail, new CorrelationId(NewOrderServlet.class.getSimpleName()), order);
					res = new ResponseDto("Order is being processed");
			        response.setStatus(HttpServletResponse.SC_ACCEPTED);		        
				} else {
					res = new ResponseDto("Order already existing");
			        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
		        response.getWriter().println(res);
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
