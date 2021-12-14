package com.example.spring.integration.reactor.kafka;

import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.messaging.support.GenericMessage;

import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

@SpringBootApplication
public class SpringIntegrationReactorKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationReactorKafkaApplication.class, args);
	}

	@Bean
	ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate(KafkaProperties kafkaProperties) {
		return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(kafkaProperties.buildProducerProperties()));
	}

	@Autowired
	@Lazy
	ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate;

	@ServiceActivator(inputChannel = "kafkaProducerChannel", outputChannel = "nullChannel")
	Mono<?> sendToKafkaReactively(String payload) {
		return this.reactiveKafkaProducerTemplate.send("testTopic", payload);
	}

	@Bean
	ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate(KafkaProperties kafkaProperties) {
		return new ReactiveKafkaConsumerTemplate<>(
				ReceiverOptions.<String, String>create(kafkaProperties.buildConsumerProperties())
						.subscription(Collections.singleton("testTopic")));
	}

	@Bean
	IntegrationFlow kafkaConsumerFlow(ReactiveKafkaConsumerTemplate<String, String> reactiveKafkaConsumerTemplate) {
		return IntegrationFlows.from(reactiveKafkaConsumerTemplate.receiveAutoAck().map(GenericMessage::new))
				.<ConsumerRecord<String, String>, String>transform(ConsumerRecord::value)
				.log(LoggingHandler.Level.DEBUG, "com.example.spring.integration.reactor.kafka")
				.<String, String>transform(String::toUpperCase)
				.channel(c -> c.queue("resultChannel"))
				.get();
	}

}
