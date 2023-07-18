package com.example.stream.rabbit.binder.observation;

import java.util.function.Consumer;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

@SpringBootApplication
public class ObservationOverStreamRabbitBinderApplication {

	private static final Log LOGGER = LogFactory.getLog(ObservationOverStreamRabbitBinderApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ObservationOverStreamRabbitBinderApplication.class, args);
	}

	@Bean
	ApplicationRunner myApplicationRunner(StreamBridge streamBridge, ObservationRegistry observationRegistry) {
		return args ->
				Observation.createNotStarted("my parent observation", observationRegistry)
						.observe(() -> {
							String data = "my data";
							LOGGER.debug("Send data to RabbitMQ: " + data);
							streamBridge.send("myQueue", data);
						});
	}

	@Bean
	public Consumer<Message<String>> myRabbitConsumer() {
		return message -> LOGGER.debug("Received message from RabbitMQ: " + message);
	}

}
