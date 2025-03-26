package com.example.quickplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.quickplay.repositories")
public class QuickplayApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuickplayApplication.class, args);
	}

}
