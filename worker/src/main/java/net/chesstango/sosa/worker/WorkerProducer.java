package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.GoFastResult;
import net.chesstango.sosa.model.WorkerStarted;
import net.chesstango.sosa.worker.configs.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class WorkerProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String identity;
    private final String gameId;


    public WorkerProducer(RabbitTemplate rabbitTemplate,
                          @Value("${app.identity}") String identity,
                          @Value("${gameId}") String gameId) {
        this.rabbitTemplate = rabbitTemplate;
        this.identity = identity;
        this.gameId = gameId;
    }

    public void send_WorkerStarted() {
        log.info("Sending WorkerStarted");
        WorkerStarted payload = new WorkerStarted(identity, gameId);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.WORKER_RESPONDS_ROUTING_KEY,
                payload
        );
    }

    public void send_GoResult(String bestMove) {
        log.info("Sending response: {}", bestMove);
        GoFastResult payload = new GoFastResult(gameId, bestMove);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.WORKER_RESPONDS_ROUTING_KEY,
                payload
        );
    }

}
