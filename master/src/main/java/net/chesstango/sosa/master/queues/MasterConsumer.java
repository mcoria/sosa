package net.chesstango.sosa.master.queues;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.SendMove;
import net.chesstango.sosa.messages.master.WorkerBusy;
import net.chesstango.sosa.messages.master.WorkerReady;
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


    public MasterConsumer(LichessClient client,
                          SosaState sosaState) {
        this.client = client;
        this.sosaState = sosaState;
    }


    @RabbitHandler
    public void handle(WorkerReady workerReady) {
        log.info("Received: {}", workerReady);

        sosaState.addAvailableWorker(workerReady.getWorkerId());
    }

    @RabbitHandler
    public void handle(WorkerBusy workerBusy) {
        log.info("Received: {}", workerBusy);

        sosaState.removeAvailableWorker(workerBusy.getWorkerId());
    }

    @RabbitHandler
    public void handle(SendMove sendMove) {
        log.info("[{}] Received: {}", sendMove.getGameId(), sendMove);

        client.gameMove(sendMove.getGameId(), sendMove.getMove());
    }
}
