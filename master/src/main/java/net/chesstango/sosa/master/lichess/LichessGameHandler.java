package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.jobs.DynamicScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final LichessClient client;

    private final DynamicScheduler dynamicScheduler;

    private final Executor ioBoundExecutor;

    private final Map<String, LichessGame> activeGames = new HashMap<>();

    public LichessGameHandler(LichessClient client,
                              DynamicScheduler dynamicScheduler,
                              @Qualifier("ioBoundExecutor") Executor ioBoundExecutor) {
        this.client = client;
        this.dynamicScheduler = dynamicScheduler;
        this.ioBoundExecutor = ioBoundExecutor;
    }

    public void gameStart(Event.GameStartEvent gameStartEvent, Supplier<Boolean> fnIsBusy) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());
        if (!fnIsBusy.get()) {
            LichessGame lichessGame = new LichessGame(client, gameStartEvent);

            activeGames.put(gameStartEvent.id(), lichessGame);

            ioBoundExecutor.execute(lichessGame);

            dynamicScheduler.scheduleGameWatchDog(gameStartEvent.id());

        } else {
            log.info("[{}] GameExecutor is busy, aborting game", gameStartEvent.id());
            client.gameAbort(gameStartEvent.id());
        }
    }

    public void gameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());
    }

    public void watchDog(String gameId) {
        log.info("[{}] WatchDog", gameId);
        LichessGame lichessGame = activeGames.get(gameId);
        if (lichessGame != null) {
            if (lichessGame.expired()) {
                log.info("[{}] Game watchdog: game is expired", gameId);
                client.gameAbort(gameId);
            }
        }
    }
}
