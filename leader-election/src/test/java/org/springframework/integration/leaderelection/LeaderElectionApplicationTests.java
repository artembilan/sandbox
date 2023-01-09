package org.springframework.integration.leaderelection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderElectionApplicationTests {

	@TempDir
	static File tmpDir;

	static TestingServer testingServer;

	@BeforeAll
	public static void setUpClass() throws Exception {
		testingServer = new TestingServer(true);
	}

	@AfterAll
	public static void tearDownClass() throws IOException {
		testingServer.stop();
	}

	@Test
	void distributedLeaderElectionOverZk() throws Exception {
		ConfigurableApplicationContext applicationContext1 =
				new SpringApplicationBuilder()
						.sources(CuratorClientConfiguration.class, LeaderElectionApplication.class)
						.properties(
								Map.of("spring.application.name", "application #1",
										"output.directory", tmpDir.getAbsolutePath()))
						.run();

		Thread.sleep(100); // Give the first application a chance to take a leadership

		ConfigurableApplicationContext applicationContext2 =
				new SpringApplicationBuilder()
						.sources(CuratorClientConfiguration.class, LeaderElectionApplication.class)
						.properties(
								Map.of("spring.application.name", "application #2",
										"output.directory", tmpDir.getAbsolutePath()))
						.run();

		Thread.sleep(1000); // Give applications a chance to generate some events.

		File eventsFile = new File(tmpDir, "applicationEvents.txt");

		List<String> lines = Files.readAllLines(eventsFile.toPath());

		assertThat(lines).containsOnly("application #1");

		applicationContext1.close();

		Thread.sleep(1000); // Give the second application a chance to obtain a leadership and generate some events.

		lines = Files.readAllLines(eventsFile.toPath());

		assertThat(lines).containsOnly("application #1", "application #2");

		applicationContext2.close();
	}

	@Configuration
	public static class CuratorClientConfiguration {

		@Bean
		public CuratorFramework client() {
			CuratorFramework client = CuratorFrameworkFactory.newClient(testingServer.getConnectString(),
					new BoundedExponentialBackoffRetry(100, 1000, 3));
			client.start();
			return client;
		}

	}

}
