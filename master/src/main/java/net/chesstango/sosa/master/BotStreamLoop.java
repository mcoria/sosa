package net.chesstango.sosa.master;// Java

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class BotStreamLoop {

    private final String botToken;

    private final LichessClientBean lichessClientBean;

    private final LichessChallengeHandler lichessChallengeHandler;

    private final LichessGameHandler lichessGameHandler;


    public BotStreamLoop(@Value("${app.botToken}") String botToken,
                         LichessClientBean lichessClientBean,
                         LichessChallengeHandler lichessChallengeHandler,
                         LichessGameHandler lichessGameHandler) {
        this.botToken = botToken;
        this.lichessClientBean = lichessClientBean;
        this.lichessChallengeHandler = lichessChallengeHandler;
        this.lichessGameHandler = lichessGameHandler;
    }

    @Async
    public void doWorkAsync() {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(botToken);

        LichessClient lichessClient = new LichessClientImp(clientAuth);

        lichessClientBean.setImp(lichessClient);

        try (Stream<Event> events = lichessClient.streamEvents()) {
            log.info("Reading Lichess Stream Events");
            events.forEach(event -> {
                log.info("event received: {}", event);
                switch (event.type()) {
                    case challenge, challengeCanceled, challengeDeclined ->
                            lichessChallengeHandler.handleChallengeEvent(event);
                    case gameStart -> lichessGameHandler.handleGameStart((Event.GameStartEvent) event);
                    case gameFinish -> lichessGameHandler.handleGameFinish((Event.GameStopEvent) event);
                }
            });
            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        }
    }
}
