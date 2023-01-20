package com.example.kinesisbinderobservationdemo;

import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.Message;

@SpringBootApplication
public class KinesisBinderObservationDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KinesisBinderObservationDemoApplication.class, args);
	}

	@Bean
	public Publisher<Message<String>> httpSupplierFlow() {
		return IntegrationFlow.from(
						WebFlux.inboundChannelAdapter("/test")
								.payloadExpression("#requestParams.name[0]"))
				.toReactivePublisher(true);
	}

	@Bean
	public Supplier<Flux<Message<String>>> httpSupplier(Publisher<Message<String>> httpRequestPublisher) {
		return () -> Flux.from(httpRequestPublisher);
	}

}
