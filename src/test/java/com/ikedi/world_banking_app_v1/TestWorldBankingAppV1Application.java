package com.ikedi.world_banking_app_v1;

import org.springframework.boot.SpringApplication;

public class TestWorldBankingAppV1Application {

	public static void main(String[] args) {
		SpringApplication.from(WorldBankingAppV1Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
