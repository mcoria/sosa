package net.chesstango.sosa.worker.lichess;

import chariot.ClientAuth;
import chariot.model.GameStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class LichessClientImp implements LichessClient {
    private final ClientAuth client;

    public LichessClientImp(ClientAuth client) {
        this.client = client;
    }

    @Override
    public synchronized Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        return client.bot().connectToGame(gameId).stream();
    }
}
