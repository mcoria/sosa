package net.chesstango.sosa.init.steps;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.master.WorkerInit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.CHESS_TANGO_EXCHANGE;
import static net.chesstango.sosa.messages.Constants.MASTER_ROUTING_KEY;

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
        WorkerInit payload = new WorkerInit(identity);
        rabbitTemplate.convertAndSend(
                CHESS_TANGO_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
        return RepeatStatus.FINISHED;
    }
}
