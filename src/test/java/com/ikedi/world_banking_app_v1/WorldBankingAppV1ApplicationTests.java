package com.ikedi.world_banking_app_v1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WorldBankingAppV1ApplicationTests {

	@Test
	void contextLoads() {
	}

}
