package net.chesstango.sosa.worker;


import net.chesstango.sosa.model.NewGame;
import net.chesstango.sosa.worker.configs.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@EnableRabbit
@Component
public class DemoConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(NewGame payload) {
        // Process message
        System.out.println("Received: " + payload);
    }
}
