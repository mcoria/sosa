package net.chesstango.sosa.master.queues;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.lichess.LichessChallenger;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.SendChallenge;
import net.chesstango.sosa.messages.master.SendMove;
import net.chesstango.sosa.messages.master.WorkerInit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.messages.Constants.MASTER_QUEUE;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
@RabbitListener(queues = MASTER_QUEUE)
public class MasterConsumer {
    private final LichessClient client;
    private final SosaState sosaState;
    private final LichessChallenger lichessChallenger;

    public MasterConsumer(LichessClient client, SosaState sosaState,
                          LichessChallenger lichessChallenger) {
        this.client = client;
        this.sosaState = sosaState;
        this.lichessChallenger = lichessChallenger;
    }

    @RabbitHandler
    public void handle(WorkerInit workerInit) {
        log.info("Received: {}", workerInit);

        sosaState.addAvailableWorker(workerInit.getWorkerId());
    }

    @RabbitHandler
    public void handle(SendChallenge sendChallenge) {
        log.info("Received: {}", sendChallenge);

        if (sosaState.isAvailableWorker(sendChallenge.getWorkerId())) {
            lichessChallenger.challengeRandomBot();
        } else {
            log.warn("Worker {} not registered", sendChallenge.getWorkerId());
        }
    }

    @RabbitHandler
    public void handle(SendMove sendMove) {
        log.info("[{}] Received: {}", sendMove.getGameId(), sendMove);

        try {

            client.gameMove(sendMove.getGameId(), sendMove.getMove());

        } catch (RuntimeException e) {
            log.error("[{}] Error sending move", sendMove.getGameId(), e);
        }
    }
}
