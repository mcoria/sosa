package net.chesstango.sosa.master;

import net.chesstango.sosa.master.configs.RabbitConfig;
import net.chesstango.sosa.model.DemoPayload;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Mauricio Coria
 */
@Service
public class DemoProducer {
    private final RabbitTemplate rabbitTemplate;

    public DemoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(DemoPayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                payload
        );
    }
}
