package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.AccountApiAuth;
import chariot.api.BotApiAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import net.chesstango.sosa.master.events.LichessTooManyGames;
import net.chesstango.sosa.master.events.LichessTooManyRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        lichessClientImp = new LichessClientImp(client, new LichessErrorParser(), applicationEventPublisher);
    }

    /**
     * NO DEBE lanzar evento LichessTooManyRequests por que el codigo es 1
     */
    @Test
    void apiCallFailedTest() {
        AccountApiAuth accountApiAuth = mock(AccountApiAuth.class);

        when(client.account()).thenReturn(accountApiAuth);
        when(accountApiAuth.profile()).thenReturn(One.fail(1, Err.from("Error message")));

        assertThrows(RuntimeException.class, () -> lichessClientImp.getProfile());

        verify(applicationEventPublisher, never()).publishEvent(any(LichessTooManyRequests.class));
    }

    /**
     * DEBE lanzar evento LichessTooManyRequests por que el codigo es 429
     */
    @Test
    void apiCallFailedTooManyRequestsTest() {
        AccountApiAuth accountApiAuth = mock(AccountApiAuth.class);

        when(client.account()).thenReturn(accountApiAuth);
        when(accountApiAuth.profile()).thenReturn(One.fail(429, Err.from("Too Many Requests")));

        assertThrows(RuntimeException.class, () -> lichessClientImp.getProfile());

        verify(applicationEventPublisher, times(1)).publishEvent(any(LichessTooManyRequests.class));
    }

    /**
     * DEBE lanzar evento LichessTooManyRequests por que el codigo es 429
     */
    @Test
    void apiCallFailedLichessTooManyGamesTest() {
        BotApiAuth botApiAuth = mock(BotApiAuth.class);

        when(client.bot()).thenReturn(botApiAuth);
        when(botApiAuth.challenge(anyString(), any())).thenReturn(One.fail(429, Err.from("{\"error\":\"You played 100 games against other bots today, please wait before challenging another bot.\",\"ratelimit\":{\"key\":\"bot.vsBot.day\",\"seconds\":27594}}")));

        // Parameters
        User user = mock(UserProfileData.class);
        when(user.id()).thenReturn("myId");
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer = mock(Consumer.class);

        // Method invocation
        Optional<Challenge> challengeOpt = lichessClientImp.challenge(user, challengeBuilderConsumer);


        // Assertions
        assertTrue(challengeOpt.isEmpty());

        verify(applicationEventPublisher, times(1)).publishEvent(any(LichessTooManyGames.class));
    }
}
