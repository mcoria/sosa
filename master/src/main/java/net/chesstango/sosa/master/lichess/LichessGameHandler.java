package net.chesstango.sosa.master.lichess;

import chariot.model.Enums;
import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.GameFinishEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.queues.MasterProducer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SosaState sosaState;

    private final MasterProducer masterProducer;

    public LichessGameHandler(ApplicationEventPublisher applicationEventPublisher,
                              SosaState sosaState,
                              MasterProducer masterProducer) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.sosaState = sosaState;
        this.masterProducer = masterProducer;
    }

    public void handleGameStart(Event.GameStartEvent gameStartEvent) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());

        GameStartEvent gameEvent = new GameStartEvent(this, gameStartEvent);

        String workerId = sosaState.takeAvailableWorker();

        String color = Enums.Color.white == gameStartEvent.game().color() ? "white" : "black";

        log.info("[{}] Worker {} assigned to game as {}", gameStartEvent.id(), workerId, color);

        masterProducer.sendGameStart(gameStartEvent.id(), workerId, color);
    }

    public void handleGameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());

        GameFinishEvent gameFinishEvent = new GameFinishEvent(this, gameStopEvent);

        //applicationEventPublisher.publishEvent(gameFinishEvent);
    }
}
