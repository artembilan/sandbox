package org.springframework.integration.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.StopWatch;

@SpringBootApplication
public class SpringIntegrationPerformanceApplication {

	public static void main(String[] args) {
		StopWatch watch = new StopWatch();
		watch.start();
		for (int i = 0; i < 100; i++) {
			System.out.println("Execution #" + i + "...");
			SpringApplication.run(SpringIntegrationPerformanceApplication.class, args).close();
		}
		watch.stop();
		System.out.println("PERFORMANCE: " + watch.getTotalTimeSeconds());
	}

	@MessagingGateway(defaultRequestChannel = "testChannel")
	interface MyGateway {

		void testIt(Object payload);

	}


}
