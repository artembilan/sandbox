package org.springframework.example.nullawayissue;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ObjectUtils;

@SpringBootApplication
public class NullAwayIssueApplication {

	@Nullable Object @Nullable [] someArgs;

	public static void main(@Nullable String @Nullable [] args) {
		if (!ObjectUtils.isEmpty(args)) {
			System.out.println("All good");
		}

		NullAwayIssueApplication application = new NullAwayIssueApplication();
		application.someArgs = args;
		if (!ObjectUtils.isEmpty(application.someArgs)) {
			System.out.println("All good again");
		}
	}

}
