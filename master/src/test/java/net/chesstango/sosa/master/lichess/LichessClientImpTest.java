package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.AccountApiAuth;
import chariot.api.BotApiAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.One;
import chariot.model.User;
import chariot.model.UserProfileData;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class LichessClientImpTest {

    @Mock
    ClientAuth client;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    LichessClientImp lichessClientImp;

    @BeforeEach
    void before() {
        lichessClientImp = new LichessClientImp(client, new LichessErrorParser());
    }

    /**
     * NO DEBE lanzar evento LichessTooManyRequestsSent por que el codigo es 1
     */
    @Test
    void apiCallFailedTest() {
        AccountApiAuth accountApiAuth = mock(AccountApiAuth.class);

        when(client.account()).thenReturn(accountApiAuth);
        when(accountApiAuth.profile()).thenReturn(One.fail(1, "Error message"));

        assertThrows(RuntimeException.class, () -> lichessClientImp.getProfile());

        verify(applicationEventPublisher, never()).publishEvent(any(LichessTooManyRequestsSent.class));
    }

    /**
     * DEBE lanzar evento LichessTooManyRequestsSent por que el codigo es 429
     */
    @Test
    void apiCallFailedTooManyRequestsTest() {
        AccountApiAuth accountApiAuth = mock(AccountApiAuth.class);

        when(client.account()).thenReturn(accountApiAuth);
        when(accountApiAuth.profile()).thenReturn(One.fail(429, "Too Many Requests"));

        // Method invocation
        TooManyRequestsException exception = assertThrows(TooManyRequestsException.class, () -> {
            lichessClientImp.getProfile();
        });


        assertEquals("Too many requests", exception.getMessage());
    }

    /**
     * DEBE lanzar evento LichessTooManyGamesPlayed
     */
    @Test
    void apiCallFailedLichessTooManyGamesTest() {
        BotApiAuth botApiAuth = mock(BotApiAuth.class);

        when(client.bot()).thenReturn(botApiAuth);
        when(botApiAuth.challenge(anyString(), any())).thenReturn(One.fail(429, "{\"error\":\"You played 100 games against other bots today, please wait before challenging another bot.\",\"ratelimit\":{\"key\":\"bot.vsBot.day\",\"seconds\":27594}}"));

        // Parameters
        User user = mock(UserProfileData.class);
        when(user.id()).thenReturn("myId");
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer = mock(Consumer.class);

        // Method invocation
        TooManyGamesException exception = assertThrows(TooManyGamesException.class, () -> {
            lichessClientImp.challenge(user, challengeBuilderConsumer);
        });


        assertEquals("Too many games", exception.getMessage());
        assertEquals(27594, exception.getRetryIn().seconds());
    }
}
