package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.model.GoResult;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.master.configs.RabbitConfig.WORKER_RESPONDS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class GameConsumer {
    private final LichessClient client;

    public GameConsumer(LichessClient client) {
        this.client = client;
    }

    @RabbitListener(queues = WORKER_RESPONDS_QUEUE)
    public void handle(GoResult goResult) {
        log.info("[{}] Moving {}", goResult.getGameId(), goResult);
        client.gameMove(goResult.getGameId(), goResult.getMove());
    }
}
