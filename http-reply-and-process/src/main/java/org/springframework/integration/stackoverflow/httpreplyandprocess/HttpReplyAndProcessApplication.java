/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.stackoverflow.httpreplyandprocess;

import java.util.Collections;

import javax.jms.ConnectionFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Artem Bilan
 */
@SpringBootApplication
public class HttpReplyAndProcessApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpReplyAndProcessApplication.class, args);
	}

	@Bean
	public IntegrationFlow replyAndProcessFlow(JmsTemplate jmsTemplate) {
		return IntegrationFlows.from(Http.inboundGateway("/replyAndProcess"))
				.publishSubscribeChannel(publishSubscribeSpec ->
						publishSubscribeSpec.subscribe(flow -> flow
								.transform((payload) -> "OK")
								.enrichHeaders(Collections.singletonMap(HttpHeaders.STATUS_CODE, HttpStatus.ACCEPTED))))
				.<String, String>transform(String::toUpperCase)
				.handle(Jms.outboundAdapter(jmsTemplate).destination("resultQueue"))
				.get();
	}

}
