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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_FINISH_EXECUTOR;
import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_LOOP_EXECUTOR;


@Component
@Slf4j
public class GamesBootStrap implements ApplicationListener<SosaEvent> {

    private final ExecutorService gameLoopTaskExecutor;

    private final ExecutorService gameFinishTaskExecutor;

    private final GameProducer gameProducer;

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    private final Map<String, Future<?>> runningGames = new ConcurrentHashMap<>();

    public GamesBootStrap(@Qualifier(GAME_LOOP_EXECUTOR) ExecutorService gameLoopTaskExecutor,
                          @Qualifier(GAME_FINISH_EXECUTOR) ExecutorService gameFinishTaskExecutor,
                          ObjectFactory<LichessGame> lichessGameBeanFactory,
                          GameProducer gameProducer) {
        this.gameLoopTaskExecutor = gameLoopTaskExecutor;
        this.gameFinishTaskExecutor = gameFinishTaskExecutor;
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

    private void startGame(Event.GameStartEvent gameStartEvent) {
        Future<?> task = gameLoopTaskExecutor.submit(() -> {
            try {
                String gameId = gameStartEvent.id();

                GameScope.setThreadConversationId(gameId);

                gameProducer.send_GameStart();

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.setGameStartEvent(gameStartEvent);

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });

        runningGames.put(gameStartEvent.id(), task);
    }

    public void workerStarted(String gameId) {
        Future<?> task = gameLoopTaskExecutor.submit(() -> {
            try {

                GameScope.setThreadConversationId(gameId);

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.run();

                gameProducer.send_GameEnd();

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });

        runningGames.put(gameId, task);
    }


    private void finishGame(Event.GameStopEvent gameStopEvent) {
        gameFinishTaskExecutor.submit(() -> {
            try {
                String gameId = gameStopEvent.id();

                GameScope.setThreadConversationId(gameId);

                Future<?> task = runningGames.remove(gameId);
                while (!task.isDone()) {
                    log.info("[{}] LichessGame loop has not finished yet", gameId);
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                GameScope.unsetThreadConversationId();
            }
        });
    }

}

