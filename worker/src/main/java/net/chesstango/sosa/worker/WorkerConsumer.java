package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.GoFast;
import net.chesstango.sosa.model.StartPosition;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
@RabbitListener(queues = "${gameId}")
public class WorkerConsumer {


    @RabbitHandler
    public void handle(StartPosition payload) {
        // Process message
        log.info("Received: {}", payload);
    }


    @RabbitHandler
    public void handle(GoFast payload) {
        // Process message
        log.info("Received: {}", payload);
    }
}
