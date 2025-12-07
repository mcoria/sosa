package net.chesstango.sosa.master.lichess;// Java

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.MasterApplication;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessMainEventsReader implements Runnable {
    private final LichessClient lichessClient;

    private final LichessChallengeHandler lichessChallengeHandler;

    private final LichessGameHandler lichessGameHandler;


    public LichessMainEventsReader(LichessClient lichessClient,
                                   LichessChallengeHandler lichessChallengeHandler,
                                   LichessGameHandler lichessGameHandler) {
        this.lichessClient = lichessClient;
        this.lichessChallengeHandler = lichessChallengeHandler;
        this.lichessGameHandler = lichessGameHandler;
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
                                lichessChallengeHandler.handleChallengeEvent(event);
                        case gameStart -> lichessGameHandler.handleGameStart((Event.GameStartEvent) event);
                        case gameFinish -> lichessGameHandler.handleGameFinish((Event.GameStopEvent) event);
                    }
                } catch (RuntimeException e) {
                    log.warn("Event processing failed", e);
                }
            });
            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        }
        MasterApplication.countDownLatch.countDown();
    }
}
