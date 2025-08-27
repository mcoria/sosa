package net.chesstango.sosa.master.lichess;


import chariot.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.position.PositionReader;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.sosa.master.GameProducer;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Getter
@Slf4j
public class LichessGame implements Runnable {
    public static final int EXPIRED_THRESHOLD = 10;

    private final LichessClient client;
    private final GameProducer gameProducer;
    private final String gameId;

    private FEN startPosition;
    private Event.GameStartEvent gameStartEvent;
    private GameStateEvent.Full gameFullEvent;
    private Color myColor;
    private int moveCounter;

    public LichessGame(LichessClient client, GameProducer gameProducer, String gameId) {
        this.client = client;
        this.gameProducer = gameProducer;
        this.gameId = gameId;
    }

    public void setGameStartEvent(Event.GameStartEvent gameStartEvent) {
        this.gameStartEvent = gameStartEvent;
        this.myColor = getMyColor(gameStartEvent);
    }


    @Override
    public void run() {
        try {
            log.info("[{}] Entering Game event loop...", gameId);
            try (Stream<GameStateEvent> gameEvents = client.streamGameStateEvent(gameId)) {
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
            } catch (RuntimeException e) {
                log.error("[{}] Game event loop failed", gameId, e);
                throw e;
            }
        } catch (RuntimeException e) {
            log.error("Error executing onlineGame", e);
            System.exit(-1);
        }
    }

    public boolean expired() {
        if (gameFullEvent != null) {
            ZonedDateTime createdAt = gameFullEvent.createdAt();
            ZonedDateTime now = ZonedDateTime.now();
            long diff = now.toEpochSecond() - createdAt.toEpochSecond();
            return diff > EXPIRED_THRESHOLD && moveCounter < 2;
        }
        return true;
    }

    private void accept(GameStateEvent.Full gameEvent) {
        log.info("[{}] GameStateEvent {}", gameId, gameEvent);
        gameFullEvent = gameEvent;

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

        gameProducer.setStartPosition(startPosition);

        accept(gameFullEvent.state());
    }


    private void accept(GameStateEvent.Chat gameEvent) {
        log.info("[{}] GameStateEvent.Chat {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.OpponentGone gameEvent) {
        log.info("[{}] GameStateEvent.OpponentGone {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.State state) {
        log.info("[{}] gameState: {}", gameId, state);

        Enums.Status status = state.status();

        switch (status) {
            case mate, resign, outoftime, stalemate, draw -> sendChatMessage("good game!!!");
            case aborted -> sendChatMessage("goodbye!!!");
            case started, created -> play(state);
            default -> log.warn("[{}] No action handler for status {}", gameId, status);
        }
    }

    private void play(GameStateEvent.State state) {
        moveCounter = state.moveList().size();

        Game game = Game.from(startPosition, state.moveList());

        PositionReader currentChessPosition = game
                .getPosition();

        if (Objects.equals(myColor, currentChessPosition.getCurrentTurn())) {
            long wTime = state.wtime().toMillis();
            long bTime = state.btime().toMillis();

            long wInc = state.winc().toMillis();
            long bInc = state.binc().toMillis();

            gameProducer.goFast((int) wTime, (int) bTime, (int) wInc, (int) bInc, state.moveList());
        }
    }

    private void sendChatMessage(String message) {
        log.info("[{}] Chat: [{}] >> {}", gameId, "chesstango", message);
        client.gameChat(gameId, message);
    }

    private Color getMyColor(Event.GameStartEvent gameStartEvent) {
        GameInfo gameInfo = gameStartEvent.game();

        if (Enums.Color.white == gameInfo.color()) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }
}
