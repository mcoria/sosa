package net.chesstango.sosa.master.lichess;

import chariot.model.Event;
import chariot.model.GameStateEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Getter
@Slf4j
public class LichessGame implements Runnable {
    public static final int EXPIRED_THRESHOLD = 10;

    private final LichessClient client;
    private final Event.GameStartEvent gameStartEvent;
    private final String gameId;

    private GameStateEvent.Full gameFullEvent;
    private int moveCounter;

    public LichessGame(LichessClient client, Event.GameStartEvent gameStartEvent) {
        this.client = client;
        this.gameStartEvent = gameStartEvent;
        this.gameId = gameStartEvent.gameId();
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
    }

    private void accept(GameStateEvent.State gameEvent) {
        log.info("[{}] GameStateEvent {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.Chat gameEvent) {
        log.info("[{}] GameStateEvent {}", gameId, gameEvent);
    }

    private void accept(GameStateEvent.OpponentGone gameEvent) {
        log.info("[{}] GameStateEvent {}", gameId, gameEvent);
    }

}
