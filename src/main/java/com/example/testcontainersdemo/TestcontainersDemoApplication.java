package com.example.testcontainersdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.MessageSourceAccessor;

@SpringBootApplication
public class TestcontainersDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestcontainersDemoApplication.class, args);
	}

	@Bean
	public MessageSourceAccessor messages(MessageSource ms) {
		return new MessageSourceAccessor(ms);
	}
}
