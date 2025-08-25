package net.chesstango.sosa.master;

import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameInfo;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Color;
import net.chesstango.sosa.master.configs.GameScope;
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

    private final ObjectFactory<LichessGame> lichessGameBeanFactory;

    public GamesBootStrap(@Qualifier(GAME_TASK_EXECUTOR) Executor gameTaskExecutor, ObjectFactory<LichessGame> lichessGameBeanFactory, DynamicScheduler dynamicScheduler) {
        this.gameTaskExecutor = gameTaskExecutor;
        this.lichessGameBeanFactory = lichessGameBeanFactory;
    }

    @Override
    public void onApplicationEvent(SosaEvent event) {
        if (event instanceof GameStartEvent gameStartEvent) {
            startGame(gameStartEvent.getGameStartEvent());
        }
    }

    private void startGame(Event.GameStartEvent gameStartEvent) {
        gameTaskExecutor.execute(() -> {
            try {
                String gameId = gameStartEvent.id();

                GameScope.setThreadConversationId(gameId);

                LichessGame lichessGame = lichessGameBeanFactory.getObject();

                lichessGame.setColor(getMyColor(gameStartEvent));

                lichessGame.run();

            } finally {
                GameScope.unsetThreadConversationId();
            }
        });
    }

    private Color getMyColor(Event.GameStartEvent gameStartEvent) {
        // Indica el estado actual del tablero
        GameInfo gameInfo = gameStartEvent.game();

        // Si el tablero indica...
        if (Enums.Color.white == gameInfo.color()) {
            // // Que juegan blancas y es mi turno, soy blancas
            return gameInfo.isMyTurn() ? Color.WHITE : Color.BLACK;
        } else {
            // // Que juegan negras y es mi turno, soy negras
            return gameInfo.isMyTurn() ? Color.BLACK : Color.WHITE;
        }
    }

}
