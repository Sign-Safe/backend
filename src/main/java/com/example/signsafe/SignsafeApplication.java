package com.example.signsafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션의 시작점(엔트리포인트)입니다.
 *
 * <p>이 클래스는 특별한 비즈니스 로직을 하지 않고,
 * 스프링이 컴포넌트(@Controller/@Service/@Repository 등)를 스캔해서
 * 애플리케이션 컨텍스트를 구성하도록 트리거하는 역할만 합니다.</p>
 */
@SpringBootApplication
public class SignsafeApplication {

	/**
	 * JVM 실행 시 가장 먼저 호출되는 main 메서드입니다.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SignsafeApplication.class, args);
	}

}
