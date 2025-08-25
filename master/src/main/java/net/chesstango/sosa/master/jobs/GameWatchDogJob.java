package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.GameScope;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ObjectProvider<LichessGame> lichessGameProvider;

    public GameWatchDogJob(LichessClient client, ObjectProvider<LichessGame> lichessGameProvider) {
        this.client = client;
        this.lichessGameProvider = lichessGameProvider;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            String gameId = context.getJobDetail().getJobDataMap().getString("gameId");
            GameScope.setThreadConversationId(gameId);
            LichessGame lichessGame = lichessGameProvider.getIfAvailable();
            if (lichessGame != null) {
                if (lichessGame.expired()) {
                    log.info("[{}] Aborting expired game", gameId);
                    client.gameAbort(gameId);
                }
            } else {
                log.warn("[{}] No LichessGame available", gameId);
            }
        } catch (Exception e) {
            log.error("Error executing GameWatchDogJob", e);
        }
    }
}
