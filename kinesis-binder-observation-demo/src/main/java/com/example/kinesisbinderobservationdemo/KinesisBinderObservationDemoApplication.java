package com.example.kinesisbinderobservationdemo;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.EmbeddedHeaderUtils;
import org.springframework.cloud.stream.binder.MessageValues;
import org.springframework.cloud.stream.binder.kinesis.properties.KinesisProducerProperties;
import org.springframework.cloud.stream.binding.NewDestinationBindingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@SpringBootApplication
public class KinesisBinderObservationDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KinesisBinderObservationDemoApplication.class, args);
	}

	@Bean
	public Consumer<Message<String>> kinesisConsumer(QueueChannel testBuffer) {
		return testBuffer::send;
	}

	@Bean
	public QueueChannel testBuffer() {
		return new QueueChannel();
	}

}
