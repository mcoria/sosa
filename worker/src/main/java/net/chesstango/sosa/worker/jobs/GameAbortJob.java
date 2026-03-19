package net.chesstango.sosa.worker.jobs;

import chariot.model.Game;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.lichess.LichessClient;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@Component
@Slf4j
public class GameAbortJob extends QuartzJobBean {

    private final LichessClient client;
    private final String gameId;

    public GameAbortJob(LichessClient client, @Value("${gameId}") String gameId) {
        this.client = client;
        this.gameId = gameId;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            log.info("[{}] Game watchdog triggered", gameId);
            Game game = client.game(gameId);
            List<String> moveList = Arrays.stream(game.moves().get().split(" ")).filter(s -> !s.isEmpty()).toList();
            if (moveList.isEmpty() || moveList.size() == 1) {
                log.info("[{}] Aborting expired game", gameId);
                client.gameAbort(gameId);
            } else {
                log.info("[{}] Game is in progress", gameId);
            }
        } catch (Exception e) {
            log.error("Error executing GameWatchDogJob", e);
        }
    }
}
