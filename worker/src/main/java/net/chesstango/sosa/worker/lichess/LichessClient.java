package net.chesstango.sosa.worker.lichess;

import chariot.model.GameStateEvent;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public interface LichessClient {
    Stream<GameStateEvent> streamGameStateEvent(String gameId);
}
