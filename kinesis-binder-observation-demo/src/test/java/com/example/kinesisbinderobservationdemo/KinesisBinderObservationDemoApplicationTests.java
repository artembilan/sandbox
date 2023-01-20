package com.example.kinesisbinderobservationdemo;

import java.util.Map;

import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.EmbeddedHeaderUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.aws.inbound.kinesis.KinesisMessageDrivenChannelAdapter;
import org.springframework.integration.aws.inbound.kinesis.KinesisShardOffset;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"cloud.aws.region.static=eu-west-2", "debug=true"})
@AutoConfigureWebTestClient
@AutoConfigureObservability
@DirtiesContext
class KinesisBinderObservationDemoApplicationTests implements LocalstackContainerTest {

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	QueueChannel kinesisReceiveChannel;

	@Test
	void tracesArePropagatedFromWebToKinesis() {
		this.webTestClient.get()
				.uri("/test?name=foo")
				.exchange()
				.expectStatus()
				.is2xxSuccessful();

		Message<?> receive = this.kinesisReceiveChannel.receive(10_000);
		assertThat(receive).isNotNull();
		assertThat(receive.getPayload()).isEqualTo("foo");
		assertThat(receive.getHeaders()).containsKey("traceparent");
	}

	@TestConfiguration
	static class Configuration {

		@Bean(destroyMethod = "")
		public AmazonKinesisAsync amazonKinesis() {
			return LocalstackContainerTest.kinesisClient();
		}

		@Bean
		public KinesisMessageDrivenChannelAdapter kinesisInboundChannelChannel(
				AmazonKinesisAsync amazonKinesis,
				QueueChannel kinesisReceiveChannel) {

			KinesisMessageDrivenChannelAdapter adapter =
					new KinesisMessageDrivenChannelAdapter(amazonKinesis, "my-event");
			adapter.setStreamInitialSequence(KinesisShardOffset.trimHorizon());
			adapter.setOutputChannel(kinesisReceiveChannel);
			adapter.setEmbeddedHeadersMapper(Configuration::unembedHeaders);
			adapter.setConverter(String::new);
			return adapter;
		}


		@Bean
		QueueChannel kinesisReceiveChannel() {
			return new QueueChannel();
		}

		private static Message<?> unembedHeaders(byte[] payload, Map<String, Object> headers) {
			try {
				return EmbeddedHeaderUtils.extractHeaders(payload).toMessage();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

	}

}
