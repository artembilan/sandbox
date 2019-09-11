package org.springframework.kafka.so57889424;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;

@SpringBootApplication
public class So57889424Application {

	public static void main(String[] args) {
		SpringApplication.run(So57889424Application.class, args);
	}

	public static final String SO57889424_TOPIC = "So57889424";

	@Bean
	public ErrorHandler seekToCurrentErrorHandler() {
		return new SeekToCurrentErrorHandler(3);
	}

	@Bean
	public AtomicInteger processCount() {
		return new AtomicInteger();
	}

	@KafkaListener(topics = SO57889424_TOPIC)
	public void process(ConsumerRecord<?, ?> record) {
		processCount().incrementAndGet();
		throw new RuntimeException("force to retry for: " + record);
	}

	@Bean
	public CountDownLatch listenerIdleLatch() {
		return new CountDownLatch(1);
	}

	@EventListener
	public void containerIdleEvenListener(ListenerContainerIdleEvent event) {
		listenerIdleLatch().countDown();
	}

}
