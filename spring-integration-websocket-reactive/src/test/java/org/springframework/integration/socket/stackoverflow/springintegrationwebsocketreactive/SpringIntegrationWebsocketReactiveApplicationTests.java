package org.springframework.integration.socket.stackoverflow.springintegrationwebsocketreactive;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Subscriber;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringIntegrationWebsocketReactiveApplicationTests {

	@LocalServerPort
	private int port;

	@Test
	public void testReactiveWebsocketsWithIntegration() throws URISyntaxException {
		WebSocketClient client = new ReactorNettyWebSocketClient();

		URI url = new URI("ws://localhost:" + this.port + "/integration");

		Flux<String> input = Flux.just("foo", "bar", "baz");

		ReplayProcessor<Object> output = ReplayProcessor.create();

		client.execute(url,
				session -> session
						.send(input.map(session::textMessage))
						.thenMany(session.receive().take(3).map(WebSocketMessage::getPayloadAsText))
						.subscribeWith(output)
						.then())
				.block(Duration.ofMillis(10_000));

		StepVerifier.create(output)
				.expectNext("FOO", "BAR", "BAZ")
				.verifyComplete();
	}

}
