package ru.sbrf.globulsblock.hackathon.harvester.moneyharvester;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class MoneyHarvesterApplication {

	public static void main(String[] args) {
		log.info("Money Harvester starting");
		SpringApplication.run(MoneyHarvesterApplication.class, args);
	}

}
