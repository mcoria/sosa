package net.chesstango.sosa.worker.steps;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.lichess.GameEventsLoopReader;
import org.jspecify.annotations.NonNull;
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

    private final GameEventsLoopReader gameEventsLoopReader;

    public ReadGameEvents(GameEventsLoopReader gameEventsLoopReader) {
        this.gameEventsLoopReader = gameEventsLoopReader;
    }

    @Override
    public @Nullable RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        gameEventsLoopReader.run();
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
