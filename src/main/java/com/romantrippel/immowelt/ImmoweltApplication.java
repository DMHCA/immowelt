package com.romantrippel.immowelt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ImmoweltApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImmoweltApplication.class, args);
	}

}
