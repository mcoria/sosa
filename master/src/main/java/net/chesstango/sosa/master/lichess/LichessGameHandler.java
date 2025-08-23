package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.BusyEvent;
import net.chesstango.sosa.master.jobs.DynamicScheduler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {


    private final ApplicationEventPublisher applicationEventPublisher;

    private final LichessClient client;

    private final DynamicScheduler dynamicScheduler;

    private final Executor ioBoundExecutor;

    private final Map<String, LichessGame> activeGames = new HashMap<>();

    public LichessGameHandler(LichessClient client,
                              DynamicScheduler dynamicScheduler,
                              ApplicationEventPublisher applicationEventPublisher,
                              @Qualifier("ioBoundExecutor") Executor ioBoundExecutor) {
        this.client = client;
        this.dynamicScheduler = dynamicScheduler;
        this.ioBoundExecutor = ioBoundExecutor;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void gameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        LichessGame lichessGame = new LichessGame(client, gameStartEvent);

        activeGames.put(gameStartEvent.id(), lichessGame);

        BusyEvent busyEvent = new BusyEvent(this, true);

        applicationEventPublisher.publishEvent(busyEvent);

        ioBoundExecutor.execute(lichessGame);

        dynamicScheduler.scheduleGameWatchDog(gameStartEvent.id());
    }

    public void gameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        BusyEvent busyEvent = new BusyEvent(this, false);

        applicationEventPublisher.publishEvent(busyEvent);
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
