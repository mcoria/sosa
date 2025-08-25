package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.GameEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    public LichessGameHandler(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void handleGameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_STARED, gameStartEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);
    }

    public void handleGameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        GameEvent gameEvent = new GameEvent(this, GameEvent.Type.GAME_FINISHED, gameStopEvent.id());

        applicationEventPublisher.publishEvent(gameEvent);
    }
}
