package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import chariot.model.GameStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessGameHandler {

    private final LichessClient client;
    private final Executor ioBoundExecutor;

    private Map<String, LichessGame> activeGames = new HashMap<>();

    public LichessGameHandler(LichessClient client, @Qualifier("ioBoundExecutor") Executor ioBoundExecutor) {
        this.client = client;
        this.ioBoundExecutor = ioBoundExecutor;
    }

    public void gameStart(Event.GameStartEvent gameStartEvent, Supplier<Boolean> fnIsBusy) {
        log.info("[{}] GameStartEvent", gameStartEvent.id());
        if (!fnIsBusy.get()) {
            LichessGame lichessGame = new LichessGame(client, gameStartEvent);

            activeGames.put(gameStartEvent.id(), lichessGame);

            ioBoundExecutor.execute(lichessGame::run);

        } else {
            log.info("[{}] GameExecutor is busy, aborting game", gameStartEvent.id());
            client.gameAbort(gameStartEvent.id());
        }
    }

    public void gameFinish(Event.GameStopEvent gameStopEvent) {
        log.info("[{}] GameStopEvent", gameStopEvent.id());
    }




}
