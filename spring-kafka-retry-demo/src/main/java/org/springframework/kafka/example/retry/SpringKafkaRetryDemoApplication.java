package org.springframework.kafka.example.retry;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ContainerPausingBackOffHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerContainerPauseService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.backoff.ExponentialBackOff;

@SpringBootApplication
public class SpringKafkaRetryDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaRetryDemoApplication.class, args);
	}

	@Bean
	DefaultErrorHandler kafkaErrorHandler(SimpleAsyncTaskSchedulerBuilder simpleAsyncTaskSchedulerBuilder) {
		ExponentialBackOff backOff = new ExponentialBackOff(100, 2);
		backOff.setMaxInterval(6_000);
		backOff.setMaxElapsedTime(20_000);
		return new DefaultErrorHandler(null, backOff,
				new ContainerPausingBackOffHandler(
						new ListenerContainerPauseService(null, simpleAsyncTaskSchedulerBuilder.build())));
	}

	@KafkaListener(topics = "${spring.kafka.template.default-topic}")
	void handleRecord(ConsumerRecord<String, String> record) {
		System.out.println("Received record: " + record);
		throw new RuntimeException("intentional for retry");
	}

}
