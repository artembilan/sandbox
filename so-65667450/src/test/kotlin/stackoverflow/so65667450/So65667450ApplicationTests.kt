package stackoverflow.so65667450

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import java.net.URI


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = ["mytopic"], bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class So65667450ApplicationTests {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<*, String>

    @LocalServerPort
    var port = 0

    val webSocketClient = ReactorNettyWebSocketClient()

    @Test
    fun contextLoads() {
        val testDataSink = Sinks.many().unicast().onBackpressureBuffer<WebSocketMessage>()
        val url = URI("ws://localhost:${port}/fromKafka")
        webSocketClient.execute(url) { session ->
            session.receive()
                .doOnNext(testDataSink::tryEmitNext)
                .then()
        }.subscribe()

        val stepVerifier =
            StepVerifier.create(
                testDataSink.asFlux()
                    .map { it.payloadAsText }
                    .doOnNext(::println))
                .expectNext("TEST DATA")
                .thenCancel()
                .verifyLater()

        this.kafkaTemplate.send("mytopic", "TEST DATA")

        stepVerifier.verify()
    }

}
