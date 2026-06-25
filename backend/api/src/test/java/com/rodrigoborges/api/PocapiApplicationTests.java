package com.rodrigoborges.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"com.jordania.api"})
class PocapiApplicationTests {
	@Test
	void contextLoads() {
	}
}
