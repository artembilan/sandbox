package org.springframework.integration.postgreschannelobservation;

import java.sql.DriverManager;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.jdbc.PgConnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.EndpointId;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.jdbc.channel.PostgresChannelMessageTableSubscriber;
import org.springframework.integration.jdbc.channel.PostgresSubscribableChannel;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.messaging.Message;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class PostgresChannelObservationApplication {

	private static final Log LOG = LogFactory.getLog(PostgresChannelObservationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PostgresChannelObservationApplication.class, args);
	}

	@Bean
	public JdbcChannelMessageStore messageStore(DataSource dataSource) {
		var messageStore = new JdbcChannelMessageStore(dataSource);
		messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
		return messageStore;
	}

	@Bean
	public PostgresChannelMessageTableSubscriber subscriber(JdbcConnectionDetails jdbcConnectionDetails) {
		return new PostgresChannelMessageTableSubscriber(
				() -> DriverManager.getConnection(
								jdbcConnectionDetails.getJdbcUrl(),
								jdbcConnectionDetails.getUsername(),
								jdbcConnectionDetails.getPassword())
						.unwrap(PgConnection.class));
	}

	@Bean
	public PostgresSubscribableChannel postgresChannel(PostgresChannelMessageTableSubscriber subscriber,
			JdbcChannelMessageStore messageStore,
			PlatformTransactionManager transactionManager) {

		var postgresSubscribableChannel = new PostgresSubscribableChannel(messageStore, "group-id", subscriber);
		postgresSubscribableChannel.setTransactionManager(transactionManager);
		return postgresSubscribableChannel;
	}

	@ServiceActivator(inputChannel = "postgresChannel")
	@EndpointId("postgresHandler")
	void handleEvent(Message<?> message) {
		LOG.info("Event received: " + message);
	}

}
