package org.springframework.integration.example.so79334692;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class So79334692ApplicationTests {

	static SshServer sftpServer;

	@TempDir
	static Path remoteDir;

	@Autowired
	PollableChannel errorChannel;

	@Autowired
	PollableChannel resultChannel;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeAll
	static void startSftpServer() throws Exception {
		sftpServer = SshServer.setUpDefaultServer();
		sftpServer.setPasswordAuthenticator((username, password, session) -> true);
		sftpServer.setPort(0);
		sftpServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Path.of("hostkey.ser")));
		SftpSubsystemFactory sftpFactory = new SftpSubsystemFactory();
		sftpServer.setSubsystemFactories(List.of(sftpFactory));
		sftpServer.setFileSystemFactory(new VirtualFileSystemFactory(remoteDir));
		sftpServer.start();
	}

	@DynamicPropertySource
	static void sftpProperties(DynamicPropertyRegistry registry) {
		registry.add("sftp.port", sftpServer::getPort);
	}

	@Test
	void verifyNoErrorsAndNoDuplicationsWithTwoSftpChannelAdapters() throws IOException {
		List<String> data = IntStream.range(0, 100).boxed().map(Objects::toString).sorted().toList();

		for (String value : data) {
			Path filePath = remoteDir.resolve(value + ".txt");
			Files.writeString(filePath, value);
		}

		List<String> resultList = new ArrayList<>();
		data.forEach(__ -> resultList.add(this.resultChannel.receive(10_000).getPayload().toString()));
		resultList.sort(String::compareTo);

		assertThat(resultList).isEqualTo(data);

		assertThat(this.errorChannel.receive(100)).isNull();

		assertThat(Files.list(remoteDir)).isEmpty();

		assertThat(this.jdbcTemplate.queryForList("SELECT * FROM int_metadata_store")).hasSize(100);
	}

	@AfterAll
	static void stopSftpServer() throws Exception {
		sftpServer.stop();
		File hostKey = new File("hostkey.ser");
		if (hostKey.exists()) {
			hostKey.delete();
		}
	}

}
