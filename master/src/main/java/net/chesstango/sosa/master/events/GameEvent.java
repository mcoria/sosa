package net.chesstango.sosa.master.events;

import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class GameEvent extends SosaEvent {

    public enum Type {
        GAME_STARED,
        GAME_FINISHED
    }

    @Getter
    private final Type type;

    @Getter
    private final String gameId;

    public GameEvent(Object source, Type type, String gameId) {
        super(source);
        this.type = type;
        this.gameId = gameId;
    }
}
