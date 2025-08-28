package net.chesstango.sosa.init;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.model.NewGame;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.init.configs.RabbitConfig.MASTER_REQUESTS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class InitConsumer {
    private final ConnectionFactory connectionFactory;

    private final PropertyWriter propertyWriter;

    @Value("${EXIT_ON_WRITE:true}")
    private boolean exitOnWrite = true;

    public InitConsumer(ConnectionFactory connectionFactory, PropertyWriter propertyWriter) {
        this.connectionFactory = connectionFactory;
        this.propertyWriter = propertyWriter;
    }

    @PostConstruct
    public void initialize() {
        try (Connection connection = connectionFactory.createConnection()) {
            if (!connection.isOpen()) {
                log.error("Something went wrong with RabbitMQ connection");
                WorkerInitApplication.finishFail();
            }
        } catch (Exception e) {
            log.error("Error initializing RabbitMQ connection", e);
            WorkerInitApplication.finishFail();
        }
    }

    @RabbitListener(queues = MASTER_REQUESTS_QUEUE)
    public void handle(NewGame payload) {
        log.info("Received: {}", payload);

        propertyWriter.writePropertyFile(payload.getGameId());

        if (exitOnWrite) {
            WorkerInitApplication.finishSuccess();
        }
    }
}
