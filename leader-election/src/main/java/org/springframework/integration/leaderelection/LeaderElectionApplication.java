package org.springframework.integration.leaderelection;

import java.io.File;

import org.apache.curator.framework.CuratorFramework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.zookeeper.config.LeaderInitiatorFactoryBean;

@SpringBootApplication
public class LeaderElectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaderElectionApplication.class, args);
	}

	@Bean
	LeaderInitiatorFactoryBean leaderInitiator(CuratorFramework client) {
		return new LeaderInitiatorFactoryBean()
				.setClient(client)
				.setPath("/someZkPath/")
				.setRole("myRole");
	}

	@Bean
	IntegrationFlow someFlow(@Value("${spring.application.name}") String applicationName,
			@Value("${output.directory}") File destinationDirectory) {

		return IntegrationFlow
				.fromSupplier(() -> applicationName,
						e -> e.role("myRole")
								.autoStartup(false)
								.poller(poller -> poller.fixedDelay(100)))
				.handle(Files.outboundAdapter(destinationDirectory)
						.fileNameGenerator(m -> "applicationEvents.txt")
						.fileExistsMode(FileExistsMode.APPEND)
						.appendNewLine(true))
				.get();
	}

}
