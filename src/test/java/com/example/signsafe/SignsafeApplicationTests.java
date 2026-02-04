package com.example.signsafe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = TestEnvInitializer.class)
class SignsafeApplicationTests {

	@Test
	void contextLoads() {
	}

}
