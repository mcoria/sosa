package net.chesstango.sosa.master;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.configs.GameScope;
import net.chesstango.sosa.master.events.GameEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    @Setter
    @Getter
    private LichessGame lichessGame;

    public GamesBootStrap(@Qualifier(GAME_TASK_EXECUTOR) Executor gameTaskExecutor) {
        this.gameTaskExecutor = gameTaskExecutor;
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

                lichessGame.run();

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });
    }

}
