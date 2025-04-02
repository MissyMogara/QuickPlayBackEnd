package com.example.quickplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class QuickplayApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(QuickplayApplication.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Application started successfully!");
	}
}
