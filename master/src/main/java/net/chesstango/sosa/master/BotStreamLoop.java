package net.chesstango.sosa.master;// Java

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.jobs.ChallengerScheduler;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessClientBean;
import net.chesstango.sosa.master.lichess.LichessClientImp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class BotStreamLoop {

    private final String bot_token;

    private final ChallengerScheduler challengerScheduler;

    private final LichessClientBean lichessClientBean;

    public BotStreamLoop(@Value("${app.bot_token}") String bot_token, ChallengerScheduler challengerScheduler, LichessClientBean lichessClientBean) {
        this.bot_token = bot_token;
        this.challengerScheduler = challengerScheduler;
        this.lichessClientBean = lichessClientBean;
    }

    @Async("ioBoundExecutor")
    public CompletableFuture<String> doWorkAsync() {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(bot_token);

        LichessClient lichessClient = new LichessClientImp(clientAuth);

        lichessClientBean.setImp(lichessClient);

        try (Stream<Event> events = lichessClient.streamEvents()) {

            challengerScheduler.scheduleOneOff();

            log.info("Reading Lichess Stream Events");
            events.forEach(event -> {
                log.info("event received: {}", event);
                switch (event.type()) {
                    case challenge -> log.info("challenge");
                    // lichessChallengeHandler.challengeCreated((Event.ChallengeCreatedEvent) event);
                    case challengeCanceled -> log.info("challenge");
                    //lichessChallengeHandler.challengeCanceled((Event.ChallengeCanceledEvent) event);
                    case challengeDeclined -> log.info("challenge");
                    //lichessChallengeHandler.challengeDeclined((Event.ChallengeDeclinedEvent) event);
                    case gameStart -> log.info("challenge");
                    //gameStart((Event.GameStartEvent) event);
                    case gameFinish -> log.info("challenge");
                    //gameStop((Event.GameStopEvent) event);
                }
            });
            log.info("main event loop finished");
        } catch (RuntimeException e) {
            log.error("main event loop failed", e);
        } finally {

        }

        // your work
        return CompletableFuture.completedFuture("done");
    }
}
