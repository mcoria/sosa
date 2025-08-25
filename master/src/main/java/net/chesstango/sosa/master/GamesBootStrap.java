package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.GameScope;
import net.chesstango.sosa.master.events.GameEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import net.chesstango.sosa.master.jobs.DynamicScheduler;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executor;

import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_TASK_EXECUTOR;

@Component
@Slf4j
public class GamesBootStrap implements ApplicationListener<SosaEvent> {

    private final Executor gameTaskExecutor;

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    public GamesBootStrap(@Qualifier(GAME_TASK_EXECUTOR) Executor gameTaskExecutor, ObjectFactory<LichessGame> lichessGameBeanFactory, DynamicScheduler dynamicScheduler) {
        this.gameTaskExecutor = gameTaskExecutor;
        this.lichessGameBeanFactory = lichessGameBeanFactory;
    }

    @Override
    public void onApplicationEvent(SosaEvent event) {
        if (event instanceof GameEvent gameEvent) {
            if (Objects.requireNonNull(gameEvent.getType()) == GameEvent.Type.GAME_STARED) {
                startGame(gameEvent.getGameId());
            }
        }
    }

    private void startGame(String gameId) {
        gameTaskExecutor.execute(() -> {
            try {
                GameScope.setThreadConversationId(gameId);

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.run();

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });
    }

}
