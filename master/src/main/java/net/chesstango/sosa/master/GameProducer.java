package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.Constants;
import net.chesstango.sosa.messages.worker.GameStart;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class GameProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String workerId;

    public GameProducer(RabbitTemplate rabbitTemplate, String workerId) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerId = workerId;
    }

    // Este mensaje va destinado a worker-init
    public void send_GameStart(String gameId, String color) {
        GameStart gameStart = new GameStart(gameId, "sosa-worker", color);
        rabbitTemplate.convertAndSend(
                Constants.SOSA_EXCHANGE,
                workerId,
                gameStart
        );
    }
}
