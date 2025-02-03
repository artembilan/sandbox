package org.springframework.kafka.example.retry;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka
class SpringKafkaRetryDemoApplicationTests {

	@Autowired
	KafkaTemplate<String, String> kafkaTemplate;

	@Test
	void recordRetried() throws InterruptedException {
		this.kafkaTemplate.sendDefault("test data");

		Thread.sleep(60_000);
	}

}
