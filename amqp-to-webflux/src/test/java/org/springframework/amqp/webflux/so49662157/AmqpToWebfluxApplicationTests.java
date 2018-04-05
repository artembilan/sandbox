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

package org.springframework.amqp.webflux.so49662157;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AmqpToWebfluxApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private Queue queueForSee;

	@Test
	public void testSeeFromAmqp() {
		this.rabbitTemplate.convertAndSend(this.queueForSee.getName(), "foo");
		this.rabbitTemplate.convertAndSend(this.queueForSee.getName(), "bar");
		this.rabbitTemplate.convertAndSend(this.queueForSee.getName(), "baz");

		Flux<String> flux1 =
				this.webTestClient.get().uri("/sseFromAmqp")
						.exchange()
						.returnResult(String.class)
						.getResponseBody();

		StepVerifier
				.create(flux1)
				.expectNext("foo", "bar", "baz")
				.thenCancel()
				.verify();
	}

}
