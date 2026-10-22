package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class LichessClientWrapperTest {

    @Mock
    private LichessClientImp lichessClientImp;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private LichessClientWrapper lichessClientWrapper;

    @BeforeEach
    void setUp() {
        lichessClientWrapper = new LichessClientWrapper(lichessClientImp, applicationEventPublisher);
    }

    @Test
    void challengeSuccess() {
        User user = mock(UserProfileData.class);
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> consumer = builder -> {};
        Challenge expectedChallenge = mock(ChallengeInfo.class);

        when(lichessClientImp.challenge(user, consumer)).thenReturn(expectedChallenge);

        Challenge actualChallenge = lichessClientWrapper.challenge(user, consumer);

        assertSame(expectedChallenge, actualChallenge);
        verify(lichessClientImp).challenge(user, consumer);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void challengeTooManyGamesException() {
        User user = mock(UserProfileData.class);
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> consumer = builder -> {};
        RetryIn retryIn = new RetryIn(60);
        TooManyGamesException exception = new TooManyGamesException(retryIn);

        when(lichessClientImp.challenge(user, consumer)).thenThrow(exception);

        TooManyGamesException thrown = assertThrows(TooManyGamesException.class,
                () -> lichessClientWrapper.challenge(user, consumer));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyGamesPlayed> captor = ArgumentCaptor.forClass(LichessTooManyGamesPlayed.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(lichessClientWrapper, captor.getValue().getSource());
        assertSame(retryIn, captor.getValue().getRetryIn());
    }

    @Test
    void challengeTooManyRequestsException() {
        User user = mock(UserProfileData.class);
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> consumer = builder -> {};
        TooManyRequestsException exception = new TooManyRequestsException();

        when(lichessClientImp.challenge(user, consumer)).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.challenge(user, consumer));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void streamEventsSuccess() {
        Stream<Event> expectedStream = Stream.empty();
        when(lichessClientImp.streamEvents()).thenReturn(expectedStream);

        Stream<Event> actualStream = lichessClientWrapper.streamEvents();

        assertSame(expectedStream, actualStream);
        verify(lichessClientImp).streamEvents();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void streamEventsTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.streamEvents()).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.streamEvents());

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void botsOnlineSuccess() {
        Stream<User> expectedStream = Stream.empty();
        when(lichessClientImp.botsOnline()).thenReturn(expectedStream);

        Stream<User> actualStream = lichessClientWrapper.botsOnline();

        assertSame(expectedStream, actualStream);
        verify(lichessClientImp).botsOnline();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void botsOnlineTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.botsOnline()).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.botsOnline());

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void meOngoingGamesSuccess() {
        Stream<GameInfo> expectedStream = Stream.empty();
        when(lichessClientImp.meOngoingGames()).thenReturn(expectedStream);

        Stream<GameInfo> actualStream = lichessClientWrapper.meOngoingGames();

        assertSame(expectedStream, actualStream);
        verify(lichessClientImp).meOngoingGames();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void meOngoingGamesTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.meOngoingGames()).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.meOngoingGames());

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void getProfileSuccess() {
        UserAuth expectedUserAuth = mock(UserProfileData.class);
        when(lichessClientImp.getProfile()).thenReturn(expectedUserAuth);

        UserAuth actualUserAuth = lichessClientWrapper.getProfile();

        assertSame(expectedUserAuth, actualUserAuth);
        verify(lichessClientImp).getProfile();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void getProfileTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.getProfile()).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.getProfile());

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void findUserSuccess() {
        UserAuth userAuth = mock(UserProfileData.class);
        Optional<UserAuth> expected = Optional.of(userAuth);
        when(lichessClientImp.findUser("bot1")).thenReturn(expected);

        Optional<UserAuth> actual = lichessClientWrapper.findUser("bot1");

        assertSame(expected, actual);
        verify(lichessClientImp).findUser("bot1");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void findUserTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.findUser("bot1")).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.findUser("bot1"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void getChallengeSuccess() {
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        Optional<ChallengeInfo> expected = Optional.of(challengeInfo);
        when(lichessClientImp.getChallenge("c123")).thenReturn(expected);

        Optional<ChallengeInfo> actual = lichessClientWrapper.getChallenge("c123");

        assertSame(expected, actual);
        verify(lichessClientImp).getChallenge("c123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void getChallengeTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.getChallenge("c123")).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.getChallenge("c123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void isOnlineSuccess() {
        User bot = mock(UserProfileData.class);
        when(lichessClientImp.isOnline(bot)).thenReturn(true);

        boolean online = lichessClientWrapper.isOnline(bot);

        assertTrue(online);
        verify(lichessClientImp).isOnline(bot);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void isOnlineTooManyRequestsException() {
        User bot = mock(UserProfileData.class);
        TooManyRequestsException exception = new TooManyRequestsException();
        when(lichessClientImp.isOnline(bot)).thenThrow(exception);

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.isOnline(bot));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void challengeAcceptSuccess() {
        lichessClientWrapper.challengeAccept("c123");

        verify(lichessClientImp).challengeAccept("c123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void challengeAcceptTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).challengeAccept("c123");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.challengeAccept("c123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void challengeDeclineSuccess() {
        lichessClientWrapper.challengeDecline("c123");

        verify(lichessClientImp).challengeDecline("c123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void challengeDeclineTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).challengeDecline("c123");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.challengeDecline("c123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void cancelChallengeSuccess() {
        lichessClientWrapper.cancelChallenge("c123");

        verify(lichessClientImp).cancelChallenge("c123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void cancelChallengeTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).cancelChallenge("c123");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.cancelChallenge("c123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void gameMoveSuccess() {
        lichessClientWrapper.gameMove("g123", "e2e4");

        verify(lichessClientImp).gameMove("g123", "e2e4");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void gameMoveTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).gameMove("g123", "e2e4");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.gameMove("g123", "e2e4"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void gameResignSuccess() {
        lichessClientWrapper.gameResign("g123");

        verify(lichessClientImp).gameResign("g123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void gameResignTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).gameResign("g123");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.gameResign("g123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void gameChatSuccess() {
        lichessClientWrapper.gameChat("g123", "good game");

        verify(lichessClientImp).gameChat("g123", "good game");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void gameChatTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).gameChat("g123", "good game");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.gameChat("g123", "good game"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }

    @Test
    void gameAbortSuccess() {
        lichessClientWrapper.gameAbort("g123");

        verify(lichessClientImp).gameAbort("g123");
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void gameAbortTooManyRequestsException() {
        TooManyRequestsException exception = new TooManyRequestsException();
        doThrow(exception).when(lichessClientImp).gameAbort("g123");

        TooManyRequestsException thrown = assertThrows(TooManyRequestsException.class,
                () -> lichessClientWrapper.gameAbort("g123"));

        assertSame(exception, thrown);

        ArgumentCaptor<LichessTooManyRequestsSent> captor = ArgumentCaptor.forClass(LichessTooManyRequestsSent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertSame(exception, captor.getValue().getSource());
    }
}
