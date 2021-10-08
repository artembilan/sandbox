# Spring Integration gateway parsing performance sample

This simple Spring Boot application contains only Spring Integration dependency and provides only simple a `@MessagingGateway` interface to parse and expose as a bean.

The goal of the sample to measure the mentioned parsing performance via currently provided `instanceSupplier` approach.
It starts an `ApplicationContext` 100 times and shows a result of the `StopWatch` around.

To compare with suggested gateway parser changes (back) to the reflection style instead of `instanceSupplier`, you need pull this [Spring Integration branch](https://github.com/spring-projects/spring-integration/tree/gateway_reflection) locally and perform `./gradlew publishToMavenLocal -x test` in Spring Integration project.
Then on this sample side, in its `pom.xml`, uncomment the `spring-integration.version` property.
Refresh project in the IDE to take fresh dependecies and run the sample again to see a difference in the SDOUT.
You can also run it via Maven: `./mvn spring-boot:run`.

On my machine it shows a performance degradation with reflection style as a half seconds.
