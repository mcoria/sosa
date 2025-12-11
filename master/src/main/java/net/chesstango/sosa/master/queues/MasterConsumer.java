package net.chesstango.sosa.master.queues;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.LichessConnected;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.LichessChallenger;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.SendChallenge;
import net.chesstango.sosa.messages.master.SendMove;
import net.chesstango.sosa.messages.master.WorkerBusy;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.chesstango.sosa.messages.Constants.MASTER_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
@RabbitListener(queues = MASTER_QUEUE)
public class MasterConsumer {
    private final LichessClient client;
    private final LichessChallenger lichessChallenger;
    private final SosaState sosaState;

    private final AtomicBoolean lichessConnected;
    private final AtomicBoolean sendRequestsAllowed;
    private final AtomicBoolean sendChallengesAllowed;

    public MasterConsumer(LichessClient client,
                          LichessChallenger lichessChallenger,
                          SosaState sosaState) {
        this.client = client;
        this.sosaState = sosaState;
        this.lichessChallenger = lichessChallenger;
        this.sendChallengesAllowed = new AtomicBoolean(true);
        this.sendRequestsAllowed = new AtomicBoolean(true);
        this.lichessConnected = new AtomicBoolean(false);
    }


    @RabbitHandler
    public void handle(SendChallenge sendChallenge) {
        log.info("Received: {}", sendChallenge);

        sosaState.addAvailableWorker(sendChallenge.getWorkerId());

        try {
            if (canSendRequests() && sendChallengesAllowed.get()) {
                lichessChallenger.challengeRandomBot();
            } else {
                log.warn("Too many games were played. Ignoring SendChallenge command.");
            }
        } catch (RuntimeException e) {
            log.error("Error challenging random bot", e);
        }
    }

    @RabbitHandler
    public void handle(WorkerBusy workerBusy) {
        log.info("Received: {}", workerBusy);

        sosaState.removeAvailableWorker(workerBusy.getWorkerId());
    }

    @RabbitHandler
    public void handle(SendMove sendMove) {
        log.info("[{}] Received: {}", sendMove.getGameId(), sendMove);

        try {
            if (canSendRequests()) {
                client.gameMove(sendMove.getGameId(), sendMove.getMove());
            } else {
                log.warn("[{}] Too many requests were sent. Ignoring SendMove command {}.", sendMove.getGameId(), sendMove.getMove());
            }
        } catch (RuntimeException e) {
            log.error("[{}] Error sending move", sendMove.getGameId(), e);
        }
    }

    @EventListener(LichessConnected.class)
    public void onLichessConnected() {
        log.warn("Lichess API: connected.");
        lichessConnected.set(true);
    }

    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequests() {
        log.warn("Lichess API: too many requests. Stop sending requests to lichess.");
        sendRequestsAllowed.set(false);
    }

    @EventListener(LichessTooManyGamesPlayed.class)
    public void onLichessTooManyGames() {
        log.warn("Lichess API: too many games. Stop sending challenges to lichess.");
        sendChallengesAllowed.set(false);
    }

    @EventListener
    public void onLichessTooManyGames(LichessTooManyExpired lichessTooManyExpired) {
        switch (lichessTooManyExpired.getExpirationType()) {
            case GAMES:
                log.info("Sending challenges again.");
                sendChallengesAllowed.set(true);
                break;
            case REQUESTS:
                log.warn("Sending requests again.");
                sendRequestsAllowed.set(true);
                break;
        }
    }

    boolean canSendRequests() {
        if (!lichessConnected.get()) {
            log.warn("Lichess API: not connected yes. Can't send requests.");
        }

        if (!sendRequestsAllowed.get()) {
            log.warn("Lichess API: too many requests. Can't send requests.");
        }

        return lichessConnected.get() && sendRequestsAllowed.get();
    }
}
