package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.GameInProgress;
import net.chesstango.sosa.worker.lichess.LichessClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class GameAbort implements Runnable {
    private final LichessClient client;
    private final String gameId;
    private volatile boolean gameInProgress;

    public GameAbort(LichessClient client, @Value("${gameId}") String gameId) {
        this.client = client;
        this.gameId = gameId;
        this.gameInProgress = false;
    }

    @Override
    public void run() {
        log.info("[{}] Executing Game Abort task", gameId);
        if (!gameInProgress) {
            log.info("[{}] Game is not in progress. Aborting.", gameId);
            client.gameAbort(gameId);
        } else {
            log.info("[{}] Game is in progress", gameId);
        }
    }

    @EventListener(GameInProgress.class)
    public void onLichessMainLoopFinished() {
        if (!gameInProgress) {
            log.info("[{}] GameInProgress event received", gameId);

            gameInProgress = true;
        }
    }
}
