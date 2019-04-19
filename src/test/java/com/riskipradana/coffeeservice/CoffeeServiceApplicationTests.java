package com.riskipradana.coffeeservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CoffeeServiceApplicationTests {

	@Autowired
	private CoffeeService service;

	@Test
	public void getOrders() {
		String coffeeId = service.getAllCoffees().blockFirst().getId();

		StepVerifier.withVirtualTime(() -> 	service.getOrders(coffeeId).take(10))
				.thenAwait(Duration.ofHours(10))
				.expectNextCount(10)
				.verifyComplete();
	}

}
