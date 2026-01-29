package com.ngaleano.lol_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LolManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LolManagerApplication.class, args);
	}

}
