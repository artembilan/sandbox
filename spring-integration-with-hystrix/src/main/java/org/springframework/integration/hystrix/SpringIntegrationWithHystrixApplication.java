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

package org.springframework.integration.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.integration.annotation.ServiceActivator;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@SpringCloudApplication
public class SpringIntegrationWithHystrixApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationWithHystrixApplication.class, args);
	}

	@ServiceActivator(inputChannel = "serviceChannel")
	@HystrixCommand(fallbackMethod = "serviceFallback")
	public String myService(String payload) {
		throw new IllegalStateException("Service not available");
	}

	public String serviceFallback(String payload) {
		return payload.toUpperCase();
	}

}
