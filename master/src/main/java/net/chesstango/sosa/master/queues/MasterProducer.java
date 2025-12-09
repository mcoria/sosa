package net.chesstango.sosa.master.queues;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.Constants;
import net.chesstango.sosa.messages.worker.GameStart;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static net.chesstango.sosa.messages.Constants.WORKER_GAMES_ROUTING_KEY;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class MasterProducer {
    private final RabbitTemplate rabbitTemplate;

    public MasterProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Este mensaje va destinado a worker-init
    public void sendGameStart(String gameId, String color) {
        GameStart gameStart = new GameStart(gameId, color);
        rabbitTemplate.convertAndSend(
                Constants.SOSA_EXCHANGE,
                WORKER_GAMES_ROUTING_KEY,
                gameStart
        );
    }
}
