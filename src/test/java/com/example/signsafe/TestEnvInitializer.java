package com.example.signsafe;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Map<String, String> env = loadDotEnv();
		setSystemPropertyIfMissing(env, "GEMINI_API_KEY");
		setSystemPropertyIfMissing(env, "DB_PASSWORD");
	}

	private Map<String, String> loadDotEnv() {
		Path envPath = Path.of(System.getProperty("user.dir"), ".env");
		if (!Files.exists(envPath)) {
			return Map.of();
		}
		try {
			List<String> lines = Files.readAllLines(envPath);
			Map<String, String> values = new HashMap<>();
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#")) {
					continue;
				}
				int idx = trimmed.indexOf('=');
				if (idx <= 0) {
					continue;
				}
				String key = trimmed.substring(0, idx).trim();
				String value = trimmed.substring(idx + 1).trim();
				values.put(key, value);
			}
			return values;
		} catch (IOException ex) {
			return Map.of();
		}
	}

	private void setSystemPropertyIfMissing(Map<String, String> env, String key) {
		if (System.getProperty(key) != null) {
			return;
		}
		String value = env.get(key);
		if (value != null && !value.isBlank()) {
			System.setProperty(key, value);
		}
	}
}
