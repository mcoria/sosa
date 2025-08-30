package net.chesstango.sosa.master;

import chariot.model.Event;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.GameScope;
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


@Component
@Slf4j
public class GamesBootStrap implements ApplicationListener<SosaEvent> {

    private final ExecutorService gameLoopTaskExecutor;

    private final GameProducer gameProducer;

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    private final Map<String, Future<?>> runningGames = Collections.synchronizedMap(new HashMap<>());

    public GamesBootStrap(@Qualifier(GAME_LOOP_EXECUTOR) ExecutorService gameLoopTaskExecutor,
                          ObjectFactory<LichessGame> lichessGameBeanFactory,
                          GameProducer gameProducer) {
        this.gameLoopTaskExecutor = gameLoopTaskExecutor;
        this.lichessGameBeanFactory = lichessGameBeanFactory;
        this.gameProducer = gameProducer;
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
        if (runningGames.containsKey(gameStartEvent.id())) {
            log.error("[{}] GameStartEvent already processed", gameId);
            throw new RuntimeException(String.format("[%s] GameStartEvent already processed", gameId));
        }

        try {
            GameScope.setThreadConversationId(gameId);

            gameProducer.send_GameStart();

            LichessGame lichessGame = lichessGameBeanFactory.getObject();

            lichessGame.setGameStartEvent(gameStartEvent);

        } finally {
            GameScope.unsetThreadConversationId();
        }
    }

    public synchronized void workerStarted(String gameId) {
        if (runningGames.containsKey(gameId)) {
            log.warn("[{}] Game is already running, ignoring message", gameId);
            throw new RuntimeException(String.format("[%s] Game is already running", gameId));
        }
        Future<?> task = gameLoopTaskExecutor.submit(() -> {
            try {

                GameScope.setThreadConversationId(gameId);

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.run();

                gameProducer.send_GameEnd();

            } catch (RuntimeException e) {
                log.error("[{}] Error executing Game", gameId, e);
                throw e;
            } finally {
                GameScope.unsetThreadConversationId();
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

