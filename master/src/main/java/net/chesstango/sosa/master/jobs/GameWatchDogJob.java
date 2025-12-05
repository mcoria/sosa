package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
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

    private final LichessClient client;

    public GameWatchDogJob(LichessClient client) {
        this.client = client;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        /*
        try {
            String gameId = context.getJobDetail().getJobDataMap().getString("gameId");
            log.info("[{}] Game watchdog triggered", gameId);
            client.meOngoingGames()
                    .stream()
                    .filter(gameInfo -> gameInfo.gameId().equals(gameId))
                    .forEach(gameInfo -> {
                        log.info("[{}] {}", gameInfo.gameId(), gameInfo);
                        Enums.Status status = gameInfo.status();
                        if (status.equals(Enums.Status.started)) {
                            log.info("[{}] Aborting expired game", gameId);
                            client.gameAbort(gameId);
                        } else {
                            log.info("[{}] Game is {}", gameInfo.gameId(), status);
                        }
                    });
        } catch (Exception e) {
            log.error("Error executing GameWatchDogJob", e);
        }

         */
    }
}
