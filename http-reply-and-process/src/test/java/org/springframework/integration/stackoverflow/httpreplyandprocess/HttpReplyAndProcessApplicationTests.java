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

import static org.assertj.core.api.Assertions.assertThat;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.http.dsl.Http;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class HttpReplyAndProcessApplicationTests {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private IntegrationFlowContext integrationFlowContext;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Test
	void testReplyAndProcess() throws JMSException {

		IntegrationFlow clientFlow =
				(flow) -> flow
						.handle(
								Http.outboundGateway(this.testRestTemplate.getRootUri() + "/replyAndProcess",
										this.testRestTemplate.getRestTemplate())
										.expectedResponseType(String.class));

		IntegrationFlowContext.IntegrationFlowRegistration registration =
				this.integrationFlowContext
						.registration(clientFlow).register();

		String reply = registration.getMessagingTemplate().convertSendAndReceive("test", String.class);
		assertThat(reply).isEqualTo("OK");

		Message result = this.jmsTemplate.receive("resultQueue");

		assertThat(result).isNotNull();

		String payload = ((TextMessage) result).getText();
		assertThat(payload).isEqualTo("TEST");
	}

}
