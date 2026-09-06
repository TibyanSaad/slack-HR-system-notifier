package com.example.slack_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlackNotificationApplication.class, args);
	}

}
