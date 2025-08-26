package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.RabbitConfig;
import net.chesstango.sosa.model.NewGame;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class NewGameProducer {
    private final RabbitTemplate rabbitTemplate;
    private final String gameId;

    public NewGameProducer(RabbitTemplate rabbitTemplate, String gameId) {
        this.rabbitTemplate = rabbitTemplate;
        this.gameId = gameId;
    }

    public void sendStartNewGame() {
        log.info("[{}] Sending NewGame event...", gameId);
        NewGame payload = new NewGame(gameId);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.NEW_GAMES_ROUTING_KEY,
                payload
        );
    }
}
