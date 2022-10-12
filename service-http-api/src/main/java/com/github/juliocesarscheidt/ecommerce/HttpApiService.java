package com.github.juliocesarscheidt.ecommerce;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class HttpApiService {

	public static void main(String[] args) throws Exception {
		QueuedThreadPool threadPool = new QueuedThreadPool(10, 1, 120);
		Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server);
        connector.setHost("0.0.0.0");
        connector.setPort(8081);
        server.setConnectors(new Connector[] {connector});

		var context = new ServletContextHandler();
		context.setContextPath("/api");
		// add paths
		context.addServlet(new ServletHolder(new NewOrderServlet()), "/order");

		server.setHandler(context);

		System.out.println("Server listening on 8080");
		
	    server.start();
	    server.join();
	}
}
