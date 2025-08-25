package net.chesstango.sosa.master.events;

import chariot.model.Event;
import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class GameStartEvent extends SosaEvent {

    @Getter
    private final Event.GameStartEvent gameStartEvent;

    public GameStartEvent(Object source, Event.GameStartEvent gameStartEvent) {
        super(source);
        this.gameStartEvent = gameStartEvent;
    }

    public String getGameId() {
        return gameStartEvent.gameId();
    }
}
