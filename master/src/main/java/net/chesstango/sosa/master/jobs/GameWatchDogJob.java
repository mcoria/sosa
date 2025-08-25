package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@Component
@Slf4j
public class GameWatchDogJob extends QuartzJobBean {

    private final Map<String, LichessGame> activeGames;

    private final LichessClient client;

    public GameWatchDogJob(Map<String, LichessGame> activeGames, LichessClient client) {
        this.activeGames = activeGames;
        this.client = client;

    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            String gameId = context.getJobDetail().getJobDataMap().getString("gameId");
            LichessGame lichessGame = activeGames.get(gameId);
            if (lichessGame != null) {
                if (lichessGame.expired()) {
                    log.info("[{}] Aborting expired game", gameId);
                    client.gameAbort(gameId);
                }
            }
        } catch (Exception e) {
            log.error("Error executing GameWatchDogJob", e);
        }
    }
}
