package com.example.signsafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 실행(@Async) 설정입니다.
 *
 * <p>분석 작업(OpenAI 호출 등)은 시간이 오래 걸릴 수 있어서 요청-응답 스레드에서
 * 바로 처리하지 않고 백그라운드 스레드풀에서 실행하도록 구성합니다.</p>
 *
 * <p>현재는 {@code AnalysisJobService#runAsync(Long)}가 이 스레드풀을 사용합니다.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * 분석 작업 전용 Executor 입니다.
	 *
	 * <p>@Async("analysisExecutor")로 지정된 메서드가 여기서 실행됩니다.</p>
	 */
	@Bean(name = "analysisExecutor")
	public Executor analysisExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("analysis-");
		executor.initialize();
		return executor;
	}
}
