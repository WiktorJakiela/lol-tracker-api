package com.wiktorjakiela.lol_tracker_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punkt startowy aplikacji, który uruchamia Spring Boot
 * i tworzy kontekst Springa.
 */
@SpringBootApplication
public class LolTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LolTrackerApiApplication.class, args);
	}

}
