package com.example.springbootreactorkafkatracing;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class SpringBootReactorKafkaTracingApplication {

	public static final String MY_TOPIC = "test-topic";

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorKafkaTracingApplication.class, args);
	}

	@Bean
	KafkaSender<Integer, String> kafkaSender(KafkaProperties kafkaProperties, ObservationRegistry observationRegistry) {
		Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
		producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				StringUtils.collectionToCommaDelimitedString(kafkaProperties.getBootstrapServers()));
		SenderOptions<Integer, String> senderOptions = SenderOptions.create(producerProperties);
		return KafkaSender.create(senderOptions.withObservation(observationRegistry));
	}

	@Bean
	KafkaReceiver<Integer, String> kafkaReceiver(KafkaProperties kafkaProperties) {
		Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
		consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
				StringUtils.collectionToCommaDelimitedString(kafkaProperties.getBootstrapServers()));
		ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.create(consumerProperties);
		return KafkaReceiver.create(receiverOptions.subscription(List.of(MY_TOPIC)));
	}

}
