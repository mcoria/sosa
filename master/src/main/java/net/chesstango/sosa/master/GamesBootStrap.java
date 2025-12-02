package net.chesstango.sosa.master;

import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.WorkerScope;
import net.chesstango.sosa.master.events.GameFinishEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_LOOP_EXECUTOR;


/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class GamesBootStrap implements ApplicationListener<SosaEvent> {

    private final SosaState sosaState;

    private final GameProducer gameProducer;

    private final ExecutorService gameLoopTaskExecutor;

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    private final Map<String, Future<?>> runningGames = Collections.synchronizedMap(new HashMap<>());

    public GamesBootStrap(SosaState sosaState, @Qualifier(GAME_LOOP_EXECUTOR) ExecutorService gameLoopTaskExecutor,
                          ObjectFactory<LichessGame> lichessGameBeanFactory,
                          GameProducer gameProducer) {
        this.sosaState = sosaState;
        this.gameProducer = gameProducer;
        this.gameLoopTaskExecutor = gameLoopTaskExecutor;
        this.lichessGameBeanFactory = lichessGameBeanFactory;
    }

    @Override
    public void onApplicationEvent(@NonNull SosaEvent event) {
        if (event instanceof GameStartEvent gameStartEvent) {
            startGame(gameStartEvent.getGameStartEvent());
        } else if (event instanceof GameFinishEvent gameFinishEvent) {
            finishGame(gameFinishEvent.getGameStopEvent());
        }
    }

    private synchronized void startGame(Event.GameStartEvent gameStartEvent) {
        String gameId = gameStartEvent.id();

        String workerId = sosaState.getNextWorker(gameId);

        try {
            WorkerScope.setThreadConversationId(workerId);

            GameInfo gameInfo = gameStartEvent.game();

            // gameInfo.color() indica con que color juego
            String color = Enums.Color.white == gameInfo.color() ? "white" : "black";

            gameProducer.send_GameStart(gameId, color);

            LichessGame lichessGame = lichessGameBeanFactory.getObject();

            lichessGame.setGameStartEvent(gameStartEvent);

        } finally {
            WorkerScope.unsetThreadConversationId();
        }
    }

    public synchronized void workerStarted(String workerId, String gameId) {
        Future<?> task = gameLoopTaskExecutor.submit(() -> {
            try {
                WorkerScope.setThreadConversationId(workerId);

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.run();

                gameProducer.send_GameEnd(gameId);

            } catch (RuntimeException e) {
                log.error("[{}] Error executing Game", gameId, e);
                throw e;
            } finally {
                WorkerScope.unsetThreadConversationId();
            }
        });
        runningGames.put(gameId, task);
    }


    private synchronized void finishGame(Event.GameStopEvent gameStopEvent) {
        String gameId = gameStopEvent.id();
        Future<?> task = runningGames.remove(gameId);
        if (task != null) {
            try {
                while (!task.isDone()) {
                    log.info("[{}] LichessGame loop has not finished yet", gameId);
                    Thread.sleep(1000);
                }
                log.info("[{}] LichessGame loop has finished, pending cleaning....", gameId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("[{}] Game worker has not started yet", gameId);
        }
    }

}

