package org.springframework.cloud.stream.example.so79382434;

import java.util.function.Consumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class So79382434Application {

	public static void main(String[] args) {
		SpringApplication.run(So79382434Application.class, args);
	}

	@Bean
	ApplicationRunner init(StreamBridge streamBridge) {
		return args -> streamBridge.send("new-price-out", "100.00");
	}

	@Bean
	Consumer<String> newPrice() {
		return data -> System.out.println("New price is: " + data);
	}

}
