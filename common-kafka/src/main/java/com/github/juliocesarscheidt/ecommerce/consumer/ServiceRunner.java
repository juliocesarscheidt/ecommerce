package com.github.juliocesarscheidt.ecommerce.consumer;

import java.util.concurrent.Executors;

public class ServiceRunner<T> {

	private final ServiceProvider<T> provider;

	public ServiceRunner(ServiceFactory<T> factory) {
		this.provider = new ServiceProvider<>(factory);
	}

	public void start(Integer threadCount) {
		// creating x threads
		var pool = Executors.newFixedThreadPool(threadCount);
		for (int i = 0; i < threadCount; i ++) {
			System.out.println("[ServiceRunner] Starting thread " + i);
			// submiting our Callable provider
			pool.submit(provider);
		}
	}
}
