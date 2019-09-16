package org.springframework.kafka.so57889424;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@SpringBootApplication
public class So57889424Application {

	public static void main(String[] args) {
		SpringApplication.run(So57889424Application.class, args);
	}

	public static final String SO57889424_TOPIC = "So57889424";

	@Autowired
	private ConcurrentKafkaListenerContainerFactory<?, ?> concurrentKafkaListenerContainerFactory;

	@PostConstruct
	public void setup() {
		this.concurrentKafkaListenerContainerFactory.setStatefulRetry(true);
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setBackOffPolicy(new ExponentialRandomBackOffPolicy());
		retryTemplate.setThrowLastExceptionOnExhausted(true);
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
		this.concurrentKafkaListenerContainerFactory.setRetryTemplate(retryTemplate);
	}

	@Bean
	public ErrorHandler seekToCurrentErrorHandler() {
		SeekToCurrentErrorHandler seekToCurrentErrorHandler = new SeekToCurrentErrorHandler(3);
		seekToCurrentErrorHandler.setCommitRecovered(true);
		return seekToCurrentErrorHandler;
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
