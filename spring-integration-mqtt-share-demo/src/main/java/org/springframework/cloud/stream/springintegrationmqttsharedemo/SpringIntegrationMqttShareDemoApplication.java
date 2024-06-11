package org.springframework.cloud.stream.springintegrationmqttsharedemo;

import org.eclipse.paho.mqttv5.client.IMqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.ClientManager;
import org.springframework.integration.mqtt.core.Mqttv5ClientManager;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
public class SpringIntegrationMqttShareDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationMqttShareDemoApplication.class, args);
	}

	@Bean
	public IntegrationFlow mqttInFlow(@Value("${mqtt.url}") String mqttUrl) {
		return IntegrationFlow.from(
						new Mqttv5PahoMessageDrivenChannelAdapter(
								mqttUrl, "subscriber-for-shared-subscription", "$share/group1/test"))
				.<byte[], String>transform(String::new)
				.handle(m -> System.out.println("Received message: " + m.getPayload()), e -> e.id("dataHandler"))
				.get();

	}

}
