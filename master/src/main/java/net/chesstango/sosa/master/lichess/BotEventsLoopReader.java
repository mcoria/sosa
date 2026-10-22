package net.chesstango.sosa.master.lichess;// Java

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class BotEventsLoopReader implements Runnable {
    private final LichessClient lichessClient;
    private final ChallengeHandler challengeHandler;
    private final GameHandler gameHandler;


    public BotEventsLoopReader(LichessClient lichessClient,
                               ChallengeHandler challengeHandler,
                               GameHandler gameHandler) {
        this.lichessClient = lichessClient;
        this.challengeHandler = challengeHandler;
        this.gameHandler = gameHandler;
    }


    @Override
    public void run() {
        try (Stream<Event> events = lichessClient.streamEvents()) {
            log.info("Reading Lichess Stream Events");

            events.forEach(event -> {
                log.info("event received: {}", event);
                try {
                    switch (event.type()) {
                        case challenge, challengeCanceled, challengeDeclined ->
                                challengeHandler.handleChallengeEvent(event);
                        case gameStart -> gameHandler.handleGameStart((Event.GameStartEvent) event);
                        case gameFinish -> gameHandler.handleGameFinish((Event.GameStopEvent) event);
                    }
                } catch (RuntimeException e) {
                    log.warn("Event processing failed", e);
                }
            });

            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        }
    }
}
