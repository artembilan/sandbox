package com.example.kinesisbinderobservationdemo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class KinesisBinderObservationDemoApplicationTests {

	@Autowired
	StreamBridge streamBridge;

	@Autowired
	QueueChannel testBuffer;

	@Test
	void tracesArePropagateOverKinesis() {
		this.streamBridge.send("my-event",
				MessageBuilder.withPayload("test data")
						.setHeader("my-name", "Test Test")
						.setHeader("spring.cloud.function.definition", "myConsumer")
						.setHeader("X-B3-TraceId", "123")
						.build());

		Message<?> receive = this.testBuffer.receive(60_000);
		assertThat(receive).isNotNull();
		assertThat(receive.getPayload()).isEqualTo("test data");
		assertThat(receive.getHeaders())
				.containsKeys("my-name", "X-B3-TraceId")
				.doesNotContainKey("spring.cloud.function.definition");
	}

}
