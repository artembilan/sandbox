package com.example.spring.integration.reactor.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(topics = "testTopic", bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class SpringIntegrationReactorKafkaApplicationTests {

	@Autowired
	MessageChannel kafkaProducerChannel;

	@Autowired
	PollableChannel resultChannel;

	@Test
	void testSendAndReceiveOverReactorKafka() {
		for (int i = 0; i < 100; i++) {
			this.kafkaProducerChannel.send(new GenericMessage<>("test#" + i));
		}

		for (int i = 0; i < 100; i++) {
			assertThat(this.resultChannel.receive(10000))
					.isNotNull()
					.extracting(Message::getPayload)
					.isEqualTo("TEST#" + i);
		}
	}

}
