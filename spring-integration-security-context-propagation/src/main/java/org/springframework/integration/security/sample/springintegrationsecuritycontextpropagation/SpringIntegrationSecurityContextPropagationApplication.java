package org.springframework.integration.security.sample.springintegrationsecuritycontextpropagation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.context.SecurityContextPropagationChannelInterceptor;

@SpringBootApplication
@EnableMethodSecurity
public class SpringIntegrationSecurityContextPropagationApplication {

	public static void main(String[] args) {
		try (ConfigurableApplicationContext applicationContext =
					 SpringApplication.run(SpringIntegrationSecurityContextPropagationApplication.class, args)) {

			MyGateway myGateway = applicationContext.getBean(MyGateway.class);

			Authentication authentication =
					UsernamePasswordAuthenticationToken.authenticated("user", "password", null);

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String securedData = myGateway.callSecuredService("secured data");

			System.out.println(securedData);
		}
	}

	@Bean
	QueueChannel myChannel() {
		return MessageChannels.queue()
//				.interceptor(new SecurityContextPropagationChannelInterceptor())
				.getObject();
	}

	@ServiceActivator(inputChannel = "myChannel")
	@PreAuthorize("isAuthenticated()")
	String securedService(String payload) {
		return "Handled '%s' by principal '%s' in thread '%s'"
				.formatted(payload,
						SecurityContextHolder.getContext().getAuthentication(),
						Thread.currentThread().getName());
	}

	@MessagingGateway(defaultRequestChannel = "myChannel")
	interface MyGateway {

		String callSecuredService(String payload);

	}

}
