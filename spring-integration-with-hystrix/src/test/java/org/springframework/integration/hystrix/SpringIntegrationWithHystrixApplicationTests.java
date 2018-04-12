package org.springframework.integration.hystrix;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringIntegrationWithHystrixApplicationTests {

	@Autowired
	private MessageChannel serviceChannel;

	@Test
	public void testHystrixCommandWithSpringIntegration() {
		MessagingTemplate messagingTemplate = new MessagingTemplate(this.serviceChannel);
		assertThat(messagingTemplate.convertSendAndReceive("foo", String.class)).isEqualTo("FOO");
	}

}
