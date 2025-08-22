package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessGameHandler;
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
public class GameWatchDogJob extends QuartzJobBean {

    private final LichessGameHandler lichessGameHandler;

    public GameWatchDogJob(LichessGameHandler lichessGameHandler) {
        this.lichessGameHandler = lichessGameHandler;
    }


    @Override
    protected void executeInternal(JobExecutionContext context) {
        String gameId = context.getJobDetail().getJobDataMap().getString("gameId");
        log.info("[{}] Gaugau", gameId);
        lichessGameHandler.watchDog(gameId);
    }
}
