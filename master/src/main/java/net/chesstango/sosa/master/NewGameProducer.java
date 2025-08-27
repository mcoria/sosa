package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.RabbitConfig;
import net.chesstango.sosa.model.NewGame;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class NewGameProducer {
    private final AmqpAdmin amqpAdmin;
    private final DirectExchange demoExchange;
    private final RabbitTemplate rabbitTemplate;
    private final String gameId;
    private Queue gameQueue;


    public NewGameProducer(AmqpAdmin amqpAdmin, DirectExchange demoExchange, RabbitTemplate rabbitTemplate, String gameId) {
        this.amqpAdmin = amqpAdmin;
        this.demoExchange = demoExchange;
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

    public void setupGameQueue() {
        log.info("[{}] Setup gameQueue and binding to exchange", gameId);
        gameQueue = new Queue(gameId, false, false, true);
        amqpAdmin.declareQueue(gameQueue);

        Binding binding = BindingBuilder.bind(gameQueue).to(demoExchange).with(gameId);
        amqpAdmin.declareBinding(binding);
    }
}
