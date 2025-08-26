package net.chesstango.sosa.worker;


import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class InitConsumer {


    @RabbitListener(queues = RabbitConfig.NEW_GAMES_QUEUE)
    public void handle(NewGame payload) {
        // Process message
        log.info("Received: {}", payload);

        NewInitApplication.countDownLatch.countDown();


    }
}
