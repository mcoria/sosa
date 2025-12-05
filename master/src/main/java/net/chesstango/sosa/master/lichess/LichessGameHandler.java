package net.chesstango.sosa.master.lichess;

import chariot.model.Enums;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.queues.MasterProducer;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final LichessClient lichessClient;

    private final SosaState sosaState;

    private final MasterProducer masterProducer;

    public LichessGameHandler(LichessClient lichessClient,
                              SosaState sosaState,
                              MasterProducer masterProducer) {
        this.lichessClient = lichessClient;
        this.sosaState = sosaState;
        this.masterProducer = masterProducer;
    }

    public void handleGameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        //GameStartEvent gameEvent = new GameStartEvent(this, gameStartEvent);

        Optional<String> workerIdOpt = sosaState.pollAvailableWorker();

        if (workerIdOpt.isPresent()) {
            String workerId = workerIdOpt.get();

            String color = Enums.Color.white == gameStartEvent.game().color() ? "white" : "black";

            log.info("[{}] Worker {} assigned for game, playing as  {}", gameStartEvent.id(), workerId, color);

            masterProducer.sendGameStart(gameStartEvent.id(), workerId, color);
        } else {
            log.warn("[{}] No available workers, aborting game", gameStartEvent.id());

            lichessClient.gameAbort(gameStartEvent.id());
        }
    }

    public void handleGameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        //GameFinishEvent gameFinishEvent = new GameFinishEvent(this, gameStopEvent);

        //applicationEventPublisher.publishEvent(gameFinishEvent);
    }
}
