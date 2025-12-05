package net.chesstango.sosa.worker.lichess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.GameStateEvent;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.LichessConnected;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessClientBean implements LichessClient {
    private final String botToken;
    private final ApplicationEventPublisher applicationEventPublisher;

    private LichessClient imp;

    public LichessClientBean(@Value("${app.botToken}") String botToken,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.botToken = botToken;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Connecting to Lichess...");

        ClientAuth clientAuth = Client.auth(botToken);

        imp = new LichessClientImp(clientAuth);

        applicationEventPublisher.publishEvent(new LichessConnected(this));
    }

    @Override
    public Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        return imp.streamGameStateEvent(gameId);
    }
}
