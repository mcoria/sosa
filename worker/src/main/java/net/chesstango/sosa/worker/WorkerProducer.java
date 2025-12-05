package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.master.SendMove;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.MASTER_ROUTING_KEY;
import static net.chesstango.sosa.messages.Constants.SOSA_EXCHANGE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class WorkerProducer {
    private final RabbitTemplate rabbitTemplate;

    private final String gameId;

    public WorkerProducer(RabbitTemplate rabbitTemplate,
                          @Value("${app.workerId}") String workerId,
                          @Value("${gameId}") String gameId) {
        this.rabbitTemplate = rabbitTemplate;
        this.gameId = gameId;
    }

    public void send_GoResult(String bestMove) {
        log.info("Sending response: {}", bestMove);
        SendMove payload = new SendMove(gameId, bestMove);
        rabbitTemplate.convertAndSend(
                SOSA_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
    }

}
