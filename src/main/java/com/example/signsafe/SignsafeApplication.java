package com.example.signsafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
// @ConfigurationPropertiesScan으로 LawApiProperties를 자동 등록
@ConfigurationPropertiesScan
public class SignsafeApplication {
	public static void main(String[] args) {
		SpringApplication.run(SignsafeApplication.class, args);
	}

}
