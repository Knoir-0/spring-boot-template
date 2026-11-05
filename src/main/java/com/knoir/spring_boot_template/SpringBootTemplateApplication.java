package com.knoir.spring_boot_template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SpringBootTemplateApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringBootTemplateApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootTemplateApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(ApplicationReadyEvent event) {
		Environment env = event.getApplicationContext().getEnvironment();
		String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
		String environment = env.getProperty("app.environment", "local");
		log.info("[Startup Success]\n[Port]:{}\n[Environment]: {}", port, environment);
	}
}
