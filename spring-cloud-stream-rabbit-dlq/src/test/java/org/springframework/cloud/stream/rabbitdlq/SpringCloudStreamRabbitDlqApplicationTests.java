package org.springframework.cloud.stream.rabbitdlq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringCloudStreamRabbitDlqApplicationTests {

	@Test
	void contextLoads(@Autowired RabbitTemplate rabbitTemplate, @Autowired CountDownLatch dlqRetryExhausted)
			throws InterruptedException {

		rabbitTemplate.convertAndSend("myDestination.consumerGroup", "test data");

		assertThat(dlqRetryExhausted.await(10, TimeUnit.SECONDS)).isTrue();
	}

	@SpringBootApplication
	public static class TestSpringCloudStreamRabbitDlqApplication {

		@Bean
		@ServiceConnection
		RabbitMQContainer rabbitContainer() {
			return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4"));
		}

		@Bean
		CountDownLatch dlqRetryExhausted() {
			return new CountDownLatch(1);
		}

		@Bean
		public Consumer<Message<String>> listener(CountDownLatch dlqRetryExhausted) {
			return message -> {
				Long retryCount = message.getHeaders().get(AmqpHeaders.RETRY_COUNT, Long.class);
				if (retryCount != null && retryCount.equals(3L)) {
					dlqRetryExhausted.countDown();
					// giving up - don't send to DLX
					throw new ImmediateAcknowledgeAmqpException("Failed after 4 attempts");
				}
				throw new AmqpRejectAndDontRequeueException("failed");
			};
		}

	}

}
