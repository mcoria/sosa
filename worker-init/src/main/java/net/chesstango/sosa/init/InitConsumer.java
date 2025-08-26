package net.chesstango.sosa.init;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.NewGame;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@EnableRabbit
@Component
@Slf4j
public class InitConsumer {
    public static final String NEW_GAMES_QUEUE = "new_games.queue";

    private final ConnectionFactory connectionFactory;

    public InitConsumer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @PostConstruct
    public void initialize() {
        try (Connection connection = connectionFactory.createConnection()) {

        } catch (Exception e) {
            log.error("Error initializing RabbitMQ connection", e);
            WorkerInitApplication.finishFail();
        }
    }

    @RabbitListener(queues = NEW_GAMES_QUEUE)
    public void handle(NewGame payload) {
        // Process message
        log.info("Received: {}", payload);

        WorkerInitApplication.finishSuccess();
    }
}
