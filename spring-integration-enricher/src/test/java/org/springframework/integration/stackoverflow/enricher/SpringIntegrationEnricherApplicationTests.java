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

package org.springframework.integration.stackoverflow.enricher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

/**
 * @author Artem Bilan
 */
@SpringBootTest
@AutoConfigureMockRestServiceServer
@AutoConfigureWebClient(registerRestTemplate = true)
class SpringIntegrationEnricherApplicationTests {

	@Autowired
	private MockRestServiceServer mockRestServiceServer;

	@Autowired
	private Function<String, String> enricherGateway;

	@Test
	void testJsonEnrichWithHttp() {
		String request =
				"{" +
						"\"name\":\"House\"," +
						"\"attributeIds\": [1,3,5]" +
						"}";

		String reply =
				"{\"attributes\": [" +
						"{\"id\": 1, \"value\":\"Waterproof\"}," +
						"{\"id\": 3, \"value\":\"SoundProof\"}," +
						"{\"id\": 5, \"value\":\"Concrete\"}" +
						"]}";

		this.mockRestServiceServer.expect(requestTo("/attributes?id=1,3,5")).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(reply, MediaType.APPLICATION_JSON));

		String resultJson = this.enricherGateway.apply(request);
		this.mockRestServiceServer.verify();

		assertThat(resultJson).isEqualTo(
				"{" +
						"\"name\":\"House\"," +
						"\"attributes\":[" +
						"{\"id\":1,\"value\":\"Waterproof\"}," +
						"{\"id\":3,\"value\":\"SoundProof\"}," +
						"{\"id\":5,\"value\":\"Concrete\"}" +
						"]}"
		);
	}

}
