package org.springframework.integration.stackoverflow.enricher;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.http.dsl.Http;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringIntegrationEnricherApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationEnricherApplication.class, args);
	}

	@Bean
	public IntegrationFlow jsonEnricherFlow(RestTemplate restTemplate) {
		return IntegrationFlows.from(Function.class)
				.transform(Transformers.fromJson(Map.class))
				.enrich((enricher) -> enricher
						.<Map<String, ?>>requestPayload((message) ->
								((List<?>) message.getPayload().get("attributeIds"))
										.stream()
										.map(Object::toString)
										.collect(Collectors.joining(",")))
						.requestSubFlow((subFlow) ->
								subFlow.handle(
										Http.outboundGateway("/attributes?id={ids}", restTemplate)
												.httpMethod(HttpMethod.GET)
												.expectedResponseType(Map.class)
												.uriVariable("ids", "payload")))
						.propertyExpression("attributes", "payload.attributes"))
				.<Map<String, ?>, Map<String, ?>>transform(
						(payload) -> {
							payload.remove("attributeIds");
							return payload;
						})
				.transform(Transformers.toJson())
				.get();
	}

}
