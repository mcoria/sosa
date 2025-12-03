package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.GoResult;
import net.chesstango.sosa.messages.master.WorkerInit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.MASTER_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
@RabbitListener(queues = MASTER_QUEUE)
public class GameConsumer {
    private final LichessClient client;
    private final SosaState sosaState;


    public GameConsumer(LichessClient client, SosaState sosaState) {
        this.client = client;
        this.sosaState = sosaState;
    }

    @RabbitHandler
    public void handle(WorkerInit workerInit) {
        try {
            log.info("WorkerInit received");

        } catch (RuntimeException e) {
            log.error("Error handling WorkerInit", e);
        }
    }

    @RabbitHandler
    public void handle(GoResult goResult) {
        try {
            log.info("[{}] GoResult {}", goResult.getGameId(), goResult);
            client.gameMove(goResult.getGameId(), goResult.getMove());
        } catch (RuntimeException e) {
            log.error("Error handling GoResult", e);
        }
    }
}
