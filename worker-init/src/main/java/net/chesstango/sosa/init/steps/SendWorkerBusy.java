package net.chesstango.sosa.init.steps;


import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.messages.master.WorkerBusy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.ExitStatus;

import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;

import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.MASTER_ROUTING_KEY;
import static net.chesstango.sosa.messages.Constants.SOSA_EXCHANGE;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class SendWorkerBusy implements Tasklet, StepExecutionListener {
    private final RabbitTemplate rabbitTemplate;
    private final String workerId;

    public SendWorkerBusy(RabbitTemplate rabbitTemplate,
                          @Value("${app.workerId}") String workerId) {
        this.rabbitTemplate = rabbitTemplate;
        this.workerId = workerId;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("[{}] Triggering challenge", workerId);
        WorkerBusy payload = new WorkerBusy(workerId);
        rabbitTemplate.convertAndSend(
                SOSA_EXCHANGE,
                MASTER_ROUTING_KEY,
                payload
        );
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

}
