package net.chesstango.sosa.master.lichess;

import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameInfo;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.queues.MasterProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class GameHandlerTest {

    @Mock
    private LichessClient lichessClient;

    @Mock
    private SosaState sosaState;

    @Mock
    private MasterProducer masterProducer;

    private GameHandler gameHandler;

    @BeforeEach
    void setUp() {
        gameHandler = new GameHandler(lichessClient, sosaState, masterProducer);
    }

    @Test
    void handleGameStart_whenGameNotInProgressAndWorkerAvailable_playsAsWhite() {
        Event.GameStartEvent gameStartEvent = mock(Event.GameStartEvent.class);
        GameInfo gameInfo = mock(GameInfo.class);

        when(gameStartEvent.id()).thenReturn("game1");
        when(gameStartEvent.game()).thenReturn(gameInfo);
        when(gameInfo.color()).thenReturn(Enums.Color.white);

        when(sosaState.isInProgressGame("game1")).thenReturn(false);
        when(sosaState.thereIsAvailableWorker()).thenReturn(true);

        gameHandler.handleGameStart(gameStartEvent);

        verify(masterProducer).sendGameStart("game1", "white");
        verify(sosaState).addGame("game1");
        verifyNoInteractions(lichessClient);
    }

    @Test
    void handleGameStart_whenGameNotInProgressAndWorkerAvailable_playsAsBlack() {
        Event.GameStartEvent gameStartEvent = mock(Event.GameStartEvent.class);
        GameInfo gameInfo = mock(GameInfo.class);

        when(gameStartEvent.id()).thenReturn("game2");
        when(gameStartEvent.game()).thenReturn(gameInfo);
        when(gameInfo.color()).thenReturn(Enums.Color.black);

        when(sosaState.isInProgressGame("game2")).thenReturn(false);
        when(sosaState.thereIsAvailableWorker()).thenReturn(true);

        gameHandler.handleGameStart(gameStartEvent);

        verify(masterProducer).sendGameStart("game2", "black");
        verify(sosaState).addGame("game2");
        verifyNoInteractions(lichessClient);
    }

    @Test
    void handleGameStart_whenGameNotInProgressAndNoWorkerAvailable_abortsGame() {
        Event.GameStartEvent gameStartEvent = mock(Event.GameStartEvent.class);

        when(gameStartEvent.id()).thenReturn("game3");
        when(sosaState.isInProgressGame("game3")).thenReturn(false);
        when(sosaState.thereIsAvailableWorker()).thenReturn(false);

        gameHandler.handleGameStart(gameStartEvent);

        verify(lichessClient).gameAbort("game3");
        verifyNoInteractions(masterProducer);
        verify(sosaState, never()).addGame(any());
    }

    @Test
    void handleGameStart_whenGameAlreadyInProgress_doesNothing() {
        Event.GameStartEvent gameStartEvent = mock(Event.GameStartEvent.class);

        when(gameStartEvent.id()).thenReturn("game4");
        when(sosaState.isInProgressGame("game4")).thenReturn(true);

        gameHandler.handleGameStart(gameStartEvent);

        verify(sosaState, never()).thereIsAvailableWorker();
        verify(sosaState, never()).addGame(any());
        verifyNoInteractions(lichessClient);
        verifyNoInteractions(masterProducer);
    }

    @Test
    void handleGameFinish_removesGameFromState() {
        Event.GameStopEvent gameStopEvent = mock(Event.GameStopEvent.class);

        when(gameStopEvent.id()).thenReturn("game5");

        gameHandler.handleGameFinish(gameStopEvent);

        verify(sosaState).removeGame("game5");
        verifyNoInteractions(lichessClient);
        verifyNoInteractions(masterProducer);
    }
}
