package net.chesstango.sosa.master.events;

import chariot.model.Event;
import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class GameFinishEvent extends SosaEvent {

    @Getter
    private final Event.GameStopEvent gameStopEvent;

    public GameFinishEvent(Object source, Event.GameStopEvent gameStopEvent) {
        super(source);
        this.gameStopEvent = gameStopEvent;
    }

    public String getGameId() {
        return gameStopEvent.id();
    }
}
