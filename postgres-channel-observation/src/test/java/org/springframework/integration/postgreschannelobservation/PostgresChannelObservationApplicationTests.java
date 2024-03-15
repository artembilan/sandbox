package org.springframework.integration.postgreschannelobservation;

import brave.handler.SpanHandler;
import brave.test.TestSpanHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.brave.bridge.BraveFinishedSpan;
import io.micrometer.tracing.exporter.FinishedSpan;
import io.micrometer.tracing.test.simple.SpansAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@AutoConfigureObservability
@ExtendWith(OutputCaptureExtension.class)
class PostgresChannelObservationApplicationTests {

	static final TestSpanHandler SPANS = new TestSpanHandler();

	@Autowired
	ObservationRegistry observationRegistry;

	@Autowired
	MessageChannel postgresChannel;

	@Test
	void observationIsPropagatedOverPostgresSubscribableChannel(CapturedOutput output) {
		Observation
				.createNotStarted("test-parent-observation", this.observationRegistry)
				.lowCardinalityKeyValue("service.name", PostgresChannelObservationApplicationTests.class.getName())
				.observe(() -> postgresChannel.send(new GenericMessage<>("test data")));

		await().untilAsserted(() -> assertThat(SPANS.spans()).hasSize(3));
		SpansAssert.assertThat(SPANS.spans().stream().map(BraveFinishedSpan::fromBrave).toList())
				.haveSameTraceId();

		assertThat(output.getOut())
				.contains(SPANS.spans().stream()
						.map(BraveFinishedSpan::fromBrave)
						.map(FinishedSpan::getTraceId)
						.findFirst()
						.get());
	}

	@TestConfiguration(proxyBeanMethods = false)
	public static class MyTestConfiguration {

		@Bean
		@ServiceConnection(name = "postgres")
		public PostgreSQLContainer<?> postgreSQLContainer() {
			return new PostgreSQLContainer<>("postgres:11");
		}

		@Bean
		public SpanHandler testSpanHandler() {
			return SPANS;
		}

	}

}
