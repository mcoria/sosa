package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.master.GoFastResult;
import net.chesstango.sosa.messages.master.WorkerReady;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.CHESS_TANGO_EXCHANGE;
import static net.chesstango.sosa.messages.Constants.MASTER_ROUTING_KEY;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class WorkerProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String workerId;
    private final String gameId;


    public WorkerProducer(RabbitTemplate rabbitTemplate,
                          @Value("${app.workerId}") String workerId,
                          @Value("${gameId}") String gameId) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerId = workerId;
        this.gameId = gameId;
    }

    public void send_WorkerStarted() {
        log.info("Sending WorkerStarted");
        WorkerReady payload = new WorkerReady(gameId, workerId);
        rabbitTemplate.convertAndSend(
                CHESS_TANGO_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
    }

    public void send_GoResult(String bestMove) {
        log.info("Sending response: {}", bestMove);
        GoFastResult payload = new GoFastResult(gameId, bestMove);
        rabbitTemplate.convertAndSend(
                CHESS_TANGO_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
    }

}
