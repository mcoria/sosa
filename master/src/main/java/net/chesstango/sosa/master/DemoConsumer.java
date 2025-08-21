package net.chesstango.sosa.master;

import net.chesstango.sosa.master.configs.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@EnableRabbit
@Component
public class DemoConsumer {

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(DemoPayload payload) {
        // Process message
        System.out.println("Received: " + payload);
    }
}
