package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final LichessClient client;

    public LichessGameHandler(LichessClient client) {
        this.client = client;
    }

    public void gameStart(Event.GameStartEvent gameStartEvent, Supplier<Boolean> fnIsBusy) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());
        if (!fnIsBusy.get()) {

        } else {
            log.info("[{}] GameExecutor is busy, aborting game", gameStartEvent.id());
            client.gameAbort(gameStartEvent.id());
        }
    }

    public void gameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());
    }
}
