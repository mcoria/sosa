package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.sosa.master.configs.RabbitConfig;
import net.chesstango.sosa.model.GoFast;
import net.chesstango.sosa.model.NewGame;
import net.chesstango.sosa.model.StartPosition;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class GameProducer {
    private final AmqpAdmin amqpAdmin;
    private final DirectExchange demoExchange;
    private final RabbitTemplate rabbitTemplate;
    private final String gameId;


    public GameProducer(AmqpAdmin amqpAdmin, DirectExchange demoExchange, RabbitTemplate rabbitTemplate, String gameId) {
        this.amqpAdmin = amqpAdmin;
        this.demoExchange = demoExchange;
        this.rabbitTemplate = rabbitTemplate;
        this.gameId = gameId;
    }

    public void setupGameQueue() {
        log.info("[{}] Setup gameQueue and binding to exchange", gameId);
        Queue gameQueue = new Queue(gameId, false, false, true);
        amqpAdmin.declareQueue(gameQueue);

        Binding binding = BindingBuilder.bind(gameQueue).to(demoExchange).with(gameId);
        amqpAdmin.declareBinding(binding);
    }

    public void sendStartNewGame() {
        NewGame newGame = new NewGame(gameId);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.MASTER_REQUESTS_ROUTING_KEY,
                newGame
        );
        log.info("[{}] NewGame sent", gameId);
    }

    public void setStartPosition(FEN fen) {
        StartPosition startPosition = new StartPosition(fen.toString());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                gameId,
                startPosition
        );
        log.info("[{}] StartPosition sent", gameId);
    }

    public void goFast(int wTime, int bTime, int wInc, int bInc, List<String> strings) {
        GoFast goFast = new GoFast(wTime, bTime, wInc, bInc, strings);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                gameId,
                goFast
        );
        log.info("[{}] GoFast sent", gameId);
    }
}
