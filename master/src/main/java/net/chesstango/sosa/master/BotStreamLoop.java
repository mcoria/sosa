package net.chesstango.sosa.master;// Java

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class BotStreamLoop {

    private final String bot_token;

    private final LichessClientBean lichessClientBean;

    private final LichessChallenger lichessChallenger;

    private final LichessChallengeHandler lichessChallengeHandler;

    private final LichessGameHandler lichessGameHandler;

    private final Set<String> ongoingGames = Collections.synchronizedSet(new HashSet<>());


    public BotStreamLoop(@Value("${app.bot_token}") String bot_token,
                         LichessClientBean lichessClientBean,
                         LichessChallenger lichessChallenger,
                         LichessChallengeHandler lichessChallengeHandler,
                         LichessGameHandler lichessGameHandler) {
        this.bot_token = bot_token;
        this.lichessClientBean = lichessClientBean;
        this.lichessChallenger = lichessChallenger;
        this.lichessChallengeHandler = lichessChallengeHandler;
        this.lichessGameHandler = lichessGameHandler;
    }

    @Async("ioBoundExecutor")
    public CompletableFuture<Void> doWorkAsync() {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(bot_token);

        LichessClient lichessClient = new LichessClientImp(clientAuth);

        lichessClientBean.setImp(lichessClient);

        try (Stream<Event> events = lichessClient.streamEvents()) {
            log.info("Reading Lichess Stream Events");
            events.forEach(event -> {
                log.info("event received: {}", event);
                switch (event.type()) {
                    case challenge, challengeCanceled, challengeDeclined ->
                            lichessChallengeHandler.handleChallengeEvent(event);
                    case gameStart -> {
                        lichessGameHandler.gameStart((Event.GameStartEvent) event);
                        ongoingGames.add(event.id());
                    }
                    case gameFinish -> {
                        lichessGameHandler.gameFinish((Event.GameStopEvent) event);
                        ongoingGames.remove(event.id());
                    }
                }
            });
            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        }

        // your work
        return CompletableFuture.completedFuture(null);
    }

    private void startChallengerIfNotBusy() {
        if (!isBusy()) {
            lichessChallenger.challengeRandom();
        }
    }

    private boolean isBusy() {
        return !ongoingGames.isEmpty();
    }
}
