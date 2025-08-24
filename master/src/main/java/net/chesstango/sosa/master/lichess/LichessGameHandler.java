package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.GameEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {
    private final LichessClient client;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Map<String, LichessGame> activeGames;

    public LichessGameHandler(LichessClient client,
                              ApplicationEventPublisher applicationEventPublisher,
                              Map<String, LichessGame> activeGames) {
        this.client = client;
        this.applicationEventPublisher = applicationEventPublisher;
        this.activeGames = activeGames;
    }

    public void handleGameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        LichessGame lichessGame = new LichessGame(client, gameStartEvent);

        activeGames.put(gameStartEvent.id(), lichessGame);

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_STARED, gameStartEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);
    }

    public void handleGameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        activeGames.remove(gameStopEvent.id());

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_FINISHED, gameStopEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);
    }
}
