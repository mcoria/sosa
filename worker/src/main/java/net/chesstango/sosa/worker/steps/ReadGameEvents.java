package net.chesstango.sosa.worker.steps;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.lichess.LichessGameEventsReader;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class ReadGameEvents implements Tasklet, StepExecutionListener {

    private final LichessGameEventsReader lichessGameEventsReader;

    public ReadGameEvents(LichessGameEventsReader lichessGameEventsReader) {
        this.lichessGameEventsReader = lichessGameEventsReader;
    }

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        lichessGameEventsReader.run();
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
