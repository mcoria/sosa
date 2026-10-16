package net.chesstango.sosa.worker.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.GameAbort;
import org.jspecify.annotations.NonNull;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@Component
@Slf4j
public class GameAbortJob extends QuartzJobBean {
    private final GameAbort gameAbort;

    public GameAbortJob(GameAbort gameAbort) {
        this.gameAbort = gameAbort;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        gameAbort.run();
    }
}
