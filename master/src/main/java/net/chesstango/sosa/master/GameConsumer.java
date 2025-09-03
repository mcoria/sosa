package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.GoFastResult;
import net.chesstango.sosa.messages.master.WorkerInit;
import net.chesstango.sosa.messages.master.WorkerReady;
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
    private final GamesBootStrap gamesBootStrap;


    public GameConsumer(LichessClient client, SosaState sosaState, GamesBootStrap gamesBootStrap) {
        this.client = client;
        this.sosaState = sosaState;
        this.gamesBootStrap = gamesBootStrap;
    }

    @RabbitHandler
    public void handle(WorkerInit workerInit) {
        try {
            log.info("WorkerInit received");
            sosaState.addAvailableWorker(workerInit.getWorkerId());
        } catch (RuntimeException e) {
            log.error("Error handling WorkerInit", e);
        }
    }


    @RabbitHandler
    public void handle(WorkerReady workerReady) {
        try {
            log.info("[{}] WorkerStarted", workerReady.getGameId());
            gamesBootStrap.workerStarted(workerReady.getWorkerId(), workerReady.getGameId());
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
