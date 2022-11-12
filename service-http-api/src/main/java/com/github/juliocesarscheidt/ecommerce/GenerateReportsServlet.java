package com.github.juliocesarscheidt.ecommerce;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.github.juliocesarscheidt.ecommerce.producer.KafkaProducerService;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GenerateReportsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final KafkaProducerService<String> batchProducer = new KafkaProducerService<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void destroy() {
		super.destroy();
		batchProducer.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String bodyRaw = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		System.out.println(bodyRaw);

		try {
			String key = "ECOMMERCE_USER_GENERATE_READING_REPORT";
			String message = "ECOMMERCE_USER_GENERATE_READING_REPORT";
			batchProducer.send("ECOMMERCE_DISPATCH_BATCH_MESSAGE", key, new CorrelationId(GenerateReportsServlet.class.getSimpleName()), message);

			ResponseDto res = new ResponseDto("Reports are being generated");
			response.setCharacterEncoding("utf-8");
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
			response.getWriter().println(res);

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
