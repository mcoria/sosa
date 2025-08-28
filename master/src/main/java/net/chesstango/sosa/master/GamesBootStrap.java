package net.chesstango.sosa.master;

import chariot.model.Event;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.GameScope;
import net.chesstango.sosa.master.events.GameFinishEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import net.chesstango.sosa.master.jobs.DynamicScheduler;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_TASK_EXECUTOR;

@Component
@Slf4j
public class GamesBootStrap implements ApplicationListener<SosaEvent> {

    private final Executor gameTaskExecutor;

    private final GameProducer gameProducer;

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    public GamesBootStrap(@Qualifier(GAME_TASK_EXECUTOR) Executor gameTaskExecutor, ObjectFactory<LichessGame> lichessGameBeanFactory, DynamicScheduler dynamicScheduler, GameProducer gameProducer) {
        this.gameTaskExecutor = gameTaskExecutor;
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
        gameTaskExecutor.execute(() -> {
            try {
                String gameId = gameStartEvent.id();

                GameScope.setThreadConversationId(gameId);

                gameProducer.setupGameQueue();

                gameProducer.sendStartNewGame();

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.setGameStartEvent(gameStartEvent);

                lichessGame.run();

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });
    }


    private void finishGame(Event.GameStopEvent gameStopEvent) {
    }

}
