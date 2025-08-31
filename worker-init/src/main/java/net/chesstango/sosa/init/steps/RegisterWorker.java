package net.chesstango.sosa.init.steps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class RegisterWorker implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Registering worker");
        return RepeatStatus.FINISHED;
    }
}
