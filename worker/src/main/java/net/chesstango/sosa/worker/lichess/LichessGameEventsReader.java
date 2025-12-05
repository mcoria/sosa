package net.chesstango.sosa.worker.lichess;


import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.position.PositionReader;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.sosa.worker.TangoController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessGameEventsReader {
    private final String gameId;
    private final LichessClient lichessClient;
    private final TangoController tangoController;

    private FEN startPosition;

    private Color myColor;

    public LichessGameEventsReader(@Value("${gameId}") String gameId,
                                   @Value("${color}") String myColor,
                                   LichessClient lichessClient,
                                   TangoController tangoController) {
        this.gameId = gameId;
        this.myColor = "white".equals(myColor) ? Color.WHITE : Color.BLACK;
        this.lichessClient = lichessClient;
        this.tangoController = tangoController;
    }

    @Async
    public void run() {
        log.info("[{}] Entering Game event loop...", gameId);
        try (Stream<GameStateEvent> gameEvents = lichessClient.streamGameStateEvent(gameId)) {
            gameEvents.forEach(gameEvent -> {
                switch (gameEvent.type()) {
                    case gameFull -> accept((GameStateEvent.Full) gameEvent);
                    case gameState -> accept((GameStateEvent.State) gameEvent);
                    case chatLine -> accept((GameStateEvent.Chat) gameEvent);
                    case opponentGone -> accept((GameStateEvent.OpponentGone) gameEvent);
                    default -> log.warn("[{}] Game event unknown failed: {}", gameId, gameEvent);
                }
            });
            log.info("[{}] Game event loop finished", gameId);
            System.exit(0);
        } catch (RuntimeException e) {
            log.error("[{}] Game event loop failed", gameId, e);
            System.exit(-1);
        }
    }

    private void accept(GameStateEvent.Full gameFullEvent) {
        log.info("[{}] GameStateEvent.Full {}", gameId, gameFullEvent);

        log.info("[{}] Playing as {}", gameId, Color.WHITE.equals(myColor) ? gameFullEvent.white() : gameFullEvent.black());

        GameType gameType = gameFullEvent.gameType();
        Variant gameVariant = gameType.variant();

        if (Variant.Basic.standard.equals(gameType.variant())) {
            this.startPosition = FEN.of(FENParser.INITIAL_FEN);
        } else if (gameVariant instanceof Variant.FromPosition fromPositionVariant) {
            Opt<String> someFen = fromPositionVariant.fen();
            this.startPosition = FEN.of(someFen.get());
        } else {
            throw new RuntimeException("GameVariant not supported variant");
        }

        tangoController.setStartPosition(startPosition);

        accept(gameFullEvent.state());
    }


    private void accept(GameStateEvent.Chat gameEvent) {
        log.info("[{}] GameStateEvent.Chat {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.OpponentGone gameEvent) {
        log.info("[{}] GameStateEvent.OpponentGone {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.State state) {
        log.info("[{}] GameStateEvent.State: {}", gameId, state);

        Enums.Status status = state.status();

        switch (status) {
            case started, created -> play(state);
            case mate, resign, outoftime, stalemate, draw -> sendChatMessage("good game!!!");
            case aborted -> sendChatMessage("goodbye!!!");
            default -> log.warn("[{}] No action handler for status {}", gameId, status);
        }
    }

    private void play(GameStateEvent.State state) {
        Game game = Game.from(startPosition, state.moveList());

        PositionReader currentChessPosition = game
                .getPosition();

        if (Objects.equals(myColor, currentChessPosition.getCurrentTurn())
                && !game.getStatus().isFinalStatus()) { // Hay situaciones en donde es DRAW y el status sigue en STARTED

            long wTime = state.wtime().toMillis();
            long bTime = state.btime().toMillis();

            long wInc = state.winc().toMillis();
            long bInc = state.binc().toMillis();

            //workerProducer.send_GoFast(gameId, (int) wTime, (int) bTime, (int) wInc, (int) bInc, state.moveList());
        }
    }

    private void sendChatMessage(String message) {
        log.info("[{}] Chat: [{}] >> {}", gameId, "chesstango", message);
        //lichessClient.gameChat(gameId, message);
    }
}
