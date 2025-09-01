package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.model.GoFastResult;
import net.chesstango.sosa.model.WorkerInitKeepAlive;
import net.chesstango.sosa.model.WorkerStarted;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.master.configs.RabbitConfig.WORKER_RESPONDS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
@RabbitListener(queues = WORKER_RESPONDS_QUEUE)
public class GameConsumer {
    private final LichessClient client;
    private final SosaState sosaState;
    private final GamesBootStrap gamesBootStrap;


    public GameConsumer(LichessClient client, SosaState sosaState, GamesBootStrap gamesBootStrap) {
        this.client = client;
        this.sosaState = sosaState;
        this.gamesBootStrap = gamesBootStrap;
    }

    @RabbitHandler
    public void handle(WorkerInitKeepAlive workerInitKeepAlive) {
        try {
            log.info("WorkerInitKeepAlive received");
            sosaState.increaseWorkerSet(workerInitKeepAlive.getWorkerId());
        } catch (RuntimeException e) {
            log.error("Error handling WorkerStarted", e);
        }
    }


    @RabbitHandler
    public void handle(WorkerStarted workerStarted) {
        try {
            log.info("[{}] WorkerStarted", workerStarted.getGameId());
            sosaState.decreaseWorkerSet(workerStarted.getWorkerId());
            gamesBootStrap.workerStarted(workerStarted.getGameId());
        } catch (RuntimeException e) {
            log.error("Error handling WorkerStarted", e);
        }
    }

    @RabbitHandler
    public void handle(GoFastResult goFastResult) {
        try {
            log.info("[{}] GoResult {}", goFastResult.getGameId(), goFastResult);
            client.gameMove(goFastResult.getGameId(), goFastResult.getMove());
        } catch (RuntimeException e) {
            log.error("Error handling GoResult", e);
        }
    }
}
