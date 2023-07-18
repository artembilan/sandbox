package org.springframework.amqp.sample.tracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAmqpObservationApplication {

	private static final Log LOGGER = LogFactory.getLog(SpringAmqpObservationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringAmqpObservationApplication.class, args);
	}

	@Bean
	ContainerCustomizer<SimpleMessageListenerContainer> simpleMessageListenerContainerContainerCustomizer() {
		return simpleMessageListenerContainer -> simpleMessageListenerContainer.setObservationEnabled(true);
	}

	@Bean
	RabbitTemplateCustomizer rabbitTemplateCustomizer() {
		return rabbitTemplate -> rabbitTemplate.setObservationEnabled(true);
	}

	@Bean
	ApplicationRunner applicationRunner(RabbitTemplate rabbitTemplate, ObservationRegistry observationRegistry) {
		return args ->
				Observation
						.createNotStarted("amqp-test-observation", observationRegistry)
						.lowCardinalityKeyValue("service.name", SpringAmqpObservationApplication.class.getName())
						.observe(() -> {
							String data = "test data";
							LOGGER.warn("Produced data: " + data);
							rabbitTemplate.convertAndSend("testQueue", data);
						});
	}

	@RabbitListener(queues = "testQueue")
	void handleAmqp(String data) {
		LOGGER.warn("Received data: " + data);
	}

}
