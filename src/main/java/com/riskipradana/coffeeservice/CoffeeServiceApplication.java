package com.riskipradana.coffeeservice;

import com.sun.jna.platform.win32.Mpr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@SpringBootApplication
public class CoffeeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeServiceApplication.class, args);
	}

}

@RestController
@RequestMapping("/coffee")
class CoffeeController {
	private final CoffeeService service;

	CoffeeController(CoffeeService service) {
		this.service = service;
	}

	@GetMapping
	public Flux<Coffee> all() {
		return service.getAllCoffees();
	}

	@GetMapping("/{id}")
	public Mono<Coffee> byId(@PathVariable String id) {
		return service.getCoffeeById(id);
	}

	@GetMapping(value = "/{id}/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<CoffeeOrder> orders(@PathVariable String id) {
		return service.getOrders(id);
	}
}


@Service
class CoffeeService {
	private final CoffeeRepository repo;

	public CoffeeService(CoffeeRepository repo) {
		this.repo = repo;
	}

	Flux<Coffee> getAllCoffees() {
		return repo.findAll();
	}

	Mono<Coffee> getCoffeeById(String coffeeId) {
		return repo.findById(coffeeId);
	}

	Flux<CoffeeOrder> getOrders(String coffeeId) {

		//dont have any subscribers yet

		return Flux.<CoffeeOrder>generate(sink -> sink.next(new CoffeeOrder(coffeeId, Instant.now())))
				.delayElements(Duration.ofSeconds(1));
	}
}

@Component
class DataLoader {
	private final CoffeeRepository repo;

	public DataLoader(CoffeeRepository repo) {
		this.repo = repo;
	}

	@PostConstruct
	private void load() {
		repo.deleteAll().thenMany(
				Flux.just("kopi luwak", "kopi tubruk", "kopi joni", "kopi kirankamu")
						.map(name -> new Coffee(UUID.randomUUID().toString(), name))
						.flatMap(repo::save))
				.thenMany(repo.findAll())
				.subscribe(System.out::println);
	}
}

interface CoffeeRepository extends ReactiveCrudRepository<Coffee, String> {}

@Data
@AllArgsConstructor
@NoArgsConstructor
class CoffeeOrder {
	private String coffeeId;
	private Instant orderTime;
}

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
class Coffee {
	@Id
	private String id;
	private String name;
}
