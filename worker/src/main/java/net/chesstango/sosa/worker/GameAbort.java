package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.GameInProgress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.gameAbort", havingValue = "true")
public class GameAbort implements Runnable {
    private final WorkerProducer workerProducer;
    private final String gameId;
    private volatile boolean gameInProgress;

    public GameAbort(WorkerProducer workerProducer, @Value("${gameId}") String gameId) {
        this.workerProducer = workerProducer;
        this.gameId = gameId;
        this.gameInProgress = false;
    }

    @Override
    public void run() {
        log.info("[{}] Executing Game Abort task", gameId);
        if (!gameInProgress) {
            log.info("[{}] Game is not in progress. Aborting.", gameId);
            workerProducer.sendAbort();
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

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("[{}] GameAbort service activated", gameId);
    }
}
