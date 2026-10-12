package net.chesstango.sosa.worker.lichess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Game;
import chariot.model.GameStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessClientBean implements LichessClient {
    private final String botToken;

    private LichessClient imp;

    public LichessClientBean(@Value("${app.botToken}") String botToken) {
        this.botToken = botToken;
    }

    public void connect() {
        log.info("Connecting to Lichess...");

        ClientAuth clientAuth = Client.auth(botToken);

        imp = new LichessClientImp(clientAuth);

        log.info("Connected to Lichess");
    }

    @Override
    public Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        return imp.streamGameStateEvent(gameId);
    }

    @Override
    public Game game(String gameId) {
        return imp.game(gameId);
    }

    @Override
    public void gameAbort(String gameId) {
        imp.gameAbort(gameId);
    }
}
