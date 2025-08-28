package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.model.GoResult;
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
    private final GamesBootStrap gamesBootStrap;


    public GameConsumer(LichessClient client, GamesBootStrap gamesBootStrap) {
        this.client = client;
        this.gamesBootStrap = gamesBootStrap;
    }

    @RabbitHandler
    public void handle(WorkerStarted workerStarted) {
        log.info("[{}] WorkerStarted", workerStarted.getGameId());
        gamesBootStrap.workerStarted(workerStarted.getGameId());
    }

    @RabbitHandler
    public void handle(GoResult goResult) {
        log.info("[{}] GoResult {}", goResult.getGameId(), goResult);
        client.gameMove(goResult.getGameId(), goResult.getMove());
    }
}
