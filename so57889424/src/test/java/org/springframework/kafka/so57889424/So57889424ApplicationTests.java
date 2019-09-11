package org.springframework.kafka.so57889424;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EmbeddedKafka(topics = So57889424Application.SO57889424_TOPIC, partitions = 6, controlledShutdown = true)
@DirtiesContext
public class So57889424ApplicationTests {

	static {
		System.setProperty(EmbeddedKafkaBroker.BROKER_LIST_PROPERTY, "spring.kafka.bootstrap-servers");
	}

	@Autowired
	private KafkaTemplate<?, String> kafkaTemplate;

	@Autowired
	private CountDownLatch listenerIdleLatch;

	@Autowired
	private AtomicInteger processCount;

	@Test
	public void testSo57889424() throws InterruptedException {
		for (int i = 0; i < 20; i++) {
			this.kafkaTemplate.send(So57889424Application.SO57889424_TOPIC, "test" + i);
		}

		this.kafkaTemplate.flush();

		assertThat(this.listenerIdleLatch.await(60, TimeUnit.SECONDS)).isTrue();

		assertThat(this.processCount.get()).isEqualTo(20 * 3);
	}

}
