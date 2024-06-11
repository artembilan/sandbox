package org.springframework.cloud.stream.springintegrationmqttsharedemo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.integration.test.mock.MockMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringIntegrationTest
@Testcontainers
@DirtiesContext
class SpringIntegrationMqttShareDemoApplicationTests {

	@Container
	static GenericContainer<?> MOSQUITTO_CONTAINER =
			new GenericContainer<>("eclipse-mosquitto:2.0.12")
					.withCommand("mosquitto -c /mosquitto-no-auth.conf")
					.withExposedPorts(1883);

	@DynamicPropertySource
	static void mqttProperties(DynamicPropertyRegistry registry) {
		registry.add("mqtt.url", () -> "tcp://localhost:" + MOSQUITTO_CONTAINER.getFirstMappedPort());
	}

	@Autowired
	MockIntegrationContext mockIntegrationContext;

	@Autowired
	MessageChannel publishToTopicChannel;

	@Test
	void testSharedSubscription() throws InterruptedException {
		ArgumentCaptor<Message<?>> messageArgumentCaptor = MockIntegration.messageArgumentCaptor();
		CountDownLatch messageHandled = new CountDownLatch(1);
		MockMessageHandler mockMessageHandler =
				MockIntegration.mockMessageHandler(messageArgumentCaptor)
						.handleNext(m -> messageHandled.countDown());

		this.mockIntegrationContext.substituteMessageHandlerFor("dataHandler", mockMessageHandler);

		String testPayload = "test data";

		this.publishToTopicChannel.send(
				MessageBuilder.withPayload(testPayload)
						.setHeader(MqttHeaders.TOPIC, "test")
						.build());

		assertThat(messageHandled.await(10, TimeUnit.SECONDS)).isTrue();

		assertThat(messageArgumentCaptor.getValue()).extracting("payload").isEqualTo(testPayload);
	}

	@TestConfiguration
	public static class MqttProducerConfiguration {

		@Bean
		@ServiceActivator(inputChannel = "publishToTopicChannel")
		Mqttv5PahoMessageHandler mqttv5PahoMessageHandler(@Value("${mqtt.url}") String mqttUrl) {
			return new Mqttv5PahoMessageHandler(mqttUrl, "publisher-for-shared-subscription");
		}

	}

}
