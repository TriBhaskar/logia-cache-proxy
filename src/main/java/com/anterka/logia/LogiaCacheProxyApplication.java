package com.anterka.logia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LogiaCacheProxyApplication {
	public static void main(String[] args) {
		// Process the arguments before starting the application
		SpringApplication application = new SpringApplication(LogiaCacheProxyApplication.class);
		application.setAddCommandLineProperties(true);
		application.run(args);
	}
}
