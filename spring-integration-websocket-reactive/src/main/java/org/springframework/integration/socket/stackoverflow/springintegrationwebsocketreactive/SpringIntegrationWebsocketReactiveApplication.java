package org.springframework.integration.socket.stackoverflow.springintegrationwebsocketreactive;

import java.util.HashMap;
import java.util.Map;

import org.reactivestreams.Publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowRegistration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringIntegrationWebsocketReactiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationWebsocketReactiveApplication.class, args);
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}

	@Bean
	public HandlerMapping handlerMapping(IntegrationFlowWebSocketHandler integrationFlowWebSocketHandler) {
		Map<String, WebSocketHandler> map = new HashMap<>();
		map.put("/integration", integrationFlowWebSocketHandler);

		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setUrlMap(map);
		mapping.setOrder(-1); // before annotated controllers
		return mapping;
	}

	@Component
	public static class IntegrationFlowWebSocketHandler implements WebSocketHandler {

		@Autowired
		private IntegrationFlowContext integrationFlowContext;

		@Override
		public Mono<Void> handle(WebSocketSession session) {

			Flux<Message<?>> input = session.receive()
					.map(m -> MessageBuilder.withPayload(m.getPayloadAsText()).setHeader("webSocketSession", session)
							.build());

			Publisher<Message<WebSocketMessage>> messagePublisher =
					IntegrationFlows.from(input)
							.<String, String>transform(String::toUpperCase)
							.<String>handle((p, h) -> ((WebSocketSession) h.get("webSocketSession")).textMessage(p))
							.toReactivePublisher();

			IntegrationFlowRegistration flowRegistration =
					this.integrationFlowContext.registration((IntegrationFlow) messagePublisher)
							.register();

			Flux<WebSocketMessage> output = Flux.from(messagePublisher)
					.map(Message::getPayload);

			return session.send(output)
					.doFinally(s -> flowRegistration.destroy());
		}

	}

}
