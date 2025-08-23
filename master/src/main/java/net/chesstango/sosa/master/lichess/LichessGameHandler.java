package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.GameEvent;
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
        this.applicationEventPublisher = applicationEventPublisher;
        this.ioBoundExecutor = ioBoundExecutor;
    }

    public void handleGameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        LichessGame lichessGame = new LichessGame(client, gameStartEvent);

        activeGames.put(gameStartEvent.id(), lichessGame);

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_STARED, gameStartEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);

        ioBoundExecutor.execute(lichessGame);

        dynamicScheduler.scheduleGameWatchDog(gameStartEvent.id());
    }

    public void handleGameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        activeGames.remove(gameStopEvent.id());

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_FINISHED, gameStopEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);
    }

    public void watchDog(String gameId) {
        LichessGame lichessGame = activeGames.get(gameId);
        if (lichessGame.expired()) {
            log.info("[{}] Game watchdog: game is expired", gameId);
            client.gameAbort(gameId);
        }
    }
}
