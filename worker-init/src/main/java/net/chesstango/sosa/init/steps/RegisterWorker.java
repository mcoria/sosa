package net.chesstango.sosa.init.steps;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.init.configs.RabbitConfig;
import net.chesstango.sosa.model.WorkerInitKeepAlive;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class RegisterWorker implements Tasklet {

    private final RabbitTemplate rabbitTemplate;

    private final String identity;

    public RegisterWorker(RabbitTemplate rabbitTemplate, @Value("${app.identity}") String identity) {
        this.rabbitTemplate = rabbitTemplate;
        this.identity = identity;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Registering worker");
        WorkerInitKeepAlive payload = new WorkerInitKeepAlive(identity);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CHESS_TANGO_EXCHANGE,
                RabbitConfig.WORKER_RESPONDS_ROUTING_KEY,
                payload
        );
        return RepeatStatus.FINISHED;
    }
}
