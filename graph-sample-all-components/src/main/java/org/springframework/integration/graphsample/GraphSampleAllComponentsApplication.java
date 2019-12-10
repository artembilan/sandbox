/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.integration.graphsample;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.MessageHandlerChain;
import org.springframework.integration.http.config.EnableIntegrationGraphController;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.store.MessageStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
@EnableIntegrationGraphController(allowedOrigins = "*")
public class GraphSampleAllComponentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphSampleAllComponentsApplication.class, args);
	}

	@Bean
	IntegrationFlow someGatewaysFlow(
			@Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor executor) {

		return IntegrationFlows.from(Http.inboundGateway("/somePath").autoStartup(false))
				.headerFilter("foo")
				.channel(c -> c.queue("queueChannel"))
				.transform(Transformers.objectToString(), e -> e.poller(p -> p.fixedDelay(1000)))
				.channel(c -> c.flux("fluxChannel"))
				.filter(payload -> true)
				.delay("delayGroup")
				.<Object, Boolean>route(p -> false,
						e -> e.id("router").defaultOutputToParentFlow())
				.handle(Http.outboundGateway("/someService"))
				.channel(c -> c.executor(executor))
				.bridge()
				.get();
	}

	@Bean
	public IntegrationFlow controlBusFlow() {
		return IntegrationFlows.from(Function.class)
				.controlBus()
				.get();
	}

	@Bean
	MessageStore messageStore() {
		return new SimpleMessageStore();
	}

	@Bean
	IntegrationFlow someSplitAggregateFlow(MessageChannel scatterChannel) {
		return IntegrationFlows.from(Http.inboundChannelAdapter("/split").autoStartup(false))
				.split()
				.enrichHeaders(h -> h.header("someHeader", "someValue"))
				.enrich(enrich -> enrich.property("someProperty", "someValue"))
				.scatterGather(scatterChannel)
				.resequence()
				.aggregate()
				.claimCheckIn(messageStore())
				.barrier(10000)
				.handle((p, h) -> p)
				.claimCheckOut(messageStore(), true)
				.log()
				.routeToRecipients(route -> route
						.recipient(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
						.recipient(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
				.get();
	}

	@Bean
	MessageChannel scatterChannel(
			@Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor executor) {

		return MessageChannels.publishSubscribe(executor)
				.applySequence(true)
				.get();
	}

	@Bean
	MessageHandler handlerForChain() {
		return new AbstractReplyProducingMessageHandler() {

			@Override
			protected Object handleRequestMessage(Message<?> requestMessage) {
				return requestMessage.getPayload();
			}

		};
	}

	@Bean
	@ServiceActivator(inputChannel = "chainInput")
	MessageHandler chain() {
		MessageHandlerChain messageHandlerChain = new MessageHandlerChain();
		messageHandlerChain.setHandlers(Collections.singletonList(handlerForChain()));
		return messageHandlerChain;
	}

}
