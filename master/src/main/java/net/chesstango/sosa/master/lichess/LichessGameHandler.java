package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class LichessGameHandler {

    private final LichessClient client;

    private final Supplier<Boolean> fnIsBusy;

    public LichessGameHandler(LichessClient client, Supplier<Boolean> fnIsBusy) {
        this.client = client;
        this.fnIsBusy = fnIsBusy;
    }

    public void gameStart(Event.GameStartEvent gameStartEvent) {
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
