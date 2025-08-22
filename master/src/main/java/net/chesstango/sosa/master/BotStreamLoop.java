package net.chesstango.sosa.master;// Java

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.jobs.ChallengerScheduler;
import net.chesstango.sosa.master.lichess.LichessChallengeHandler;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessClientBean;
import net.chesstango.sosa.master.lichess.LichessClientImp;
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

    private final ChallengerTask challengerTask;

    private final LichessChallengeHandler lichessChallengeHandler;

    private final Set<String> ongoingGames = Collections.synchronizedSet(new HashSet<>());


    public BotStreamLoop(@Value("${app.bot_token}") String bot_token,
                         ChallengerScheduler challengerScheduler,
                         LichessClientBean lichessClientBean,
                         ChallengerTask challengerTask) {
        this.bot_token = bot_token;
        this.lichessClientBean = lichessClientBean;
        this.challengerTask = challengerTask;
        this.lichessChallengeHandler = new LichessChallengeHandler(lichessClientBean, this::isBusy, this::startChallengerIfNotBusy);
    }

    @Async("ioBoundExecutor")
    public CompletableFuture<Void> doWorkAsync() {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(bot_token);

        LichessClient lichessClient = new LichessClientImp(clientAuth);

        lichessClientBean.setImp(lichessClient);

        try (Stream<Event> events = lichessClient.streamEvents()) {

            challengerTask.doWorkAsync();

            log.info("Reading Lichess Stream Events");
            events.forEach(event -> {
                log.info("event received: {}", event);
                switch (event.type()) {
                    case challenge -> lichessChallengeHandler.challengeCreated((Event.ChallengeCreatedEvent) event);
                    case challengeCanceled ->
                            lichessChallengeHandler.challengeCanceled((Event.ChallengeCanceledEvent) event);
                    case challengeDeclined ->
                            lichessChallengeHandler.challengeDeclined((Event.ChallengeDeclinedEvent) event);
                    case gameStart -> gameStart((Event.GameStartEvent) event);
                    case gameFinish -> gameFinish((Event.GameStopEvent) event);
                }
            });
            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        }

        // your work
        return CompletableFuture.completedFuture(null);
    }

    private void gameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());
        if (!isBusy()) {
            ongoingGames.add(gameStartEvent.id());
        } else {
            log.info("[{}] GameExecutor is busy, aborting game", gameStartEvent.id());
            lichessClientBean.gameAbort(gameStartEvent.id());
        }
    }

    private void gameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());
        ongoingGames.remove(gameStopEvent.id());
        startChallengerIfNotBusy();
    }

    private void startChallengerIfNotBusy() {
        if (!isBusy()) {
            challengerTask.doWorkAsync();
        }
    }

    private boolean isBusy() {
        return !ongoingGames.isEmpty();
    }
}
