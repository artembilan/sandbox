package stackoverflow.so65667450

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import reactor.core.publisher.Sinks


@SpringBootApplication
class So65667450Application {

    val sink = Sinks.many().multicast().onBackpressureBuffer<String>()

    @KafkaListener(topics = ["mytopic"], groupId = "test-consumer-group")
    fun receiveData(message: String) {
        this.sink.tryEmitNext(message)
    }

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = mapOf("/fromKafka" to
                WebSocketHandler { session ->
                    session.send(this.sink.asFlux().map(session::textMessage))
                })
        return SimpleUrlHandlerMapping(map, -1)
    }

    fun main(args: Array<String>) {
        runApplication<So65667450Application>(*args)
    }

}

