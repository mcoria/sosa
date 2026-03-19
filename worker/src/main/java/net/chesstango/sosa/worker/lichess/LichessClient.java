package net.chesstango.sosa.worker.lichess;

import chariot.model.Game;
import chariot.model.GameStateEvent;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public interface LichessClient {
    Stream<GameStateEvent> streamGameStateEvent(String gameId);

    Game game(String gameId);

    void gameAbort(String gameId);
}
