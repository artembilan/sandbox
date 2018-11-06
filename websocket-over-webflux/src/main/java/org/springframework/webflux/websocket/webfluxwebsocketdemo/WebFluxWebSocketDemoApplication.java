/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.webflux.websocket.webfluxwebsocketdemo;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Artem Bilan
 * @author Christian Davatz
 */
@SpringBootApplication
@RestController
public class WebFluxWebSocketDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebFluxWebSocketDemoApplication.class, args);
	}

	@Autowired
	private WebSocketClient webSocketClient;

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getStreaming() {
		EmitterProcessor<String> output = EmitterProcessor.create();

		Mono<Void> sessionMono =
				this.webSocketClient.execute(URI.create("ws://localhost:8080/echo"),
						session -> session.receive()
								.map(WebSocketMessage::getPayloadAsText)
								.subscribeWith(output)
								.then());

		return output.doOnSubscribe(s -> sessionMono.subscribe());
	}

	@GetMapping(path = "/remote", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getRemoteStreaming() {
		Flux<String> input = Flux.<String>generate(sink -> sink
				.next(String.format("{got remote message: 'message', date: '%s' }", new Date())))
				.delayElements(Duration.ofSeconds(1));

		EmitterProcessor<String> output = EmitterProcessor.create();

		Mono<Void> sessionMono =
				this.webSocketClient.execute(URI.create("ws://echo.websocket.org"),
						session -> session.send(
								input.map(session::textMessage))
								.thenMany(session.receive()
										.map(WebSocketMessage::getPayloadAsText)
										.subscribeWith(output))
								.then());

		return output.doOnSubscribe(s -> sessionMono.subscribe());
	}

	@Bean
	public HandlerMapping webSocketMapping() {
		Map<String, WebSocketHandler> map = new HashMap<>();

		map.put("/echo", session ->
				session.send(
						Flux.just("foo", "bar", "baz", "cux")
								.map(session::textMessage)));

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setUrlMap(map);
		mapping.setOrder(1);
		return mapping;
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

	@Bean
	public WebSocketClient webSocketClient() {
		return new ReactorNettyWebSocketClient();
	}

}
