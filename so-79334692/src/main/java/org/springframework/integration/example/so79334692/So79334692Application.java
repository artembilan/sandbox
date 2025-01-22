package org.springframework.integration.example.so79334692;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.sshd.sftp.client.SftpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.advice.ExpressionEvaluatingRequestHandlerAdvice;
import org.springframework.integration.jdbc.metadata.JdbcMetadataStore;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpPersistentAcceptOnceFileListFilter;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.messaging.Message;

@SpringBootApplication
public class So79334692Application {

	public static void main(String[] args) {
		SpringApplication.run(So79334692Application.class, args);
	}

	@Bean
	QueueChannel errorChannel() {
		return new QueueChannel();
	}

	@Bean
	SessionFactory<SftpClient.DirEntry> sftpSessionFactory(@Value("${sftp.port}") int sftpPort) {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(false);
		factory.setHost("localhost");
		factory.setPort(sftpPort);
		factory.setUser("test");
		factory.setPassword("test");
		factory.setAllowUnknownKeys(true);
		CachingSessionFactory<SftpClient.DirEntry> cachingSessionFactory = new CachingSessionFactory<>(factory);
		cachingSessionFactory.setTestSession(true);
		return cachingSessionFactory;
	}

	@Bean
	SftpRemoteFileTemplate sftpRemoteFileTemplate(SessionFactory<SftpClient.DirEntry> sftpSessionFactory) {
		return new SftpRemoteFileTemplate(sftpSessionFactory);
	}

	@Bean
	JdbcMetadataStore jdbcMetadataStore(DataSource dataSource) {
		return new JdbcMetadataStore(dataSource);
	}

	@Bean
	SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnceFileListFilter(
			ConcurrentMetadataStore metadataStore) {

		return new SftpPersistentAcceptOnceFileListFilter(metadataStore, "so-79334692");
	}

	@Bean
	IntegrationFlow sftpInboundFlow1(RemoteFileTemplate<SftpClient.DirEntry> sftpRemoteFileTemplate,
			SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnceFileListFilter) {

		return IntegrationFlow.from(Sftp.inboundStreamingAdapter(sftpRemoteFileTemplate)
								.filter(sftpPersistentAcceptOnceFileListFilter)
								.remoteDirectory("/"),
						e -> e.poller(pollerFactory ->
								pollerFactory.fixedDelay(1000)
										.maxMessagesPerPoll(10)
										.transactional()))
				.log(m -> "From 'sftpInboundFlow1': " + m)
				.channel("fileProcessInput")
				.get();
	}

	@Bean
	IntegrationFlow sftpInboundFlow2(RemoteFileTemplate<SftpClient.DirEntry> sftpRemoteFileTemplate,
			SftpPersistentAcceptOnceFileListFilter sftpPersistentAcceptOnceFileListFilter) {

		return IntegrationFlow.from(Sftp.inboundStreamingAdapter(sftpRemoteFileTemplate)
								.filter(sftpPersistentAcceptOnceFileListFilter)
								.remoteDirectory("/"),
						e -> e.poller(pollerFactory ->
								pollerFactory.fixedDelay(1000)
										.maxMessagesPerPoll(10)
										.transactional()))
				.log(m -> "From 'sftpInboundFlow2': " + m)
				.channel("fileProcessInput")
				.get();
	}

	@Bean
	public ExpressionEvaluatingRequestHandlerAdvice afterAdvice(
			RemoteFileTemplate<SftpClient.DirEntry> sftpRemoteFileTemplate) {

		ExpressionEvaluatingRequestHandlerAdvice advice = new ExpressionEvaluatingRequestHandlerAdvice();
		advice.setOnSuccessExpression(
				new FunctionExpression<Message<?>>(message ->
						sftpRemoteFileTemplate.remove(message.getHeaders().get(FileHeaders.REMOTE_DIRECTORY) +
								sftpRemoteFileTemplate.getRemoteFileSeparator()
								+ message.getHeaders().get(FileHeaders.REMOTE_FILE))));
		return advice;
	}

	@Bean
	IntegrationFlow processFileFlow(MethodInterceptor afterAdvice) {
		return IntegrationFlow.from("fileProcessInput")
				.transform(Transformers.fromStream(StandardCharsets.UTF_8.name()))
				.<String>handle((p, h) -> p, e -> e.advice(afterAdvice))
				.channel(c -> c.queue("resultChannel"))
				.get();
	}

}
