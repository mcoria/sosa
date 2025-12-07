package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import chariot.model.User;
import chariot.model.UserAuth;
import chariot.model.UserProfileData;
import net.chesstango.sosa.master.SosaState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class LichessChallengerBotTest {
    @Mock
    LichessClient client;

    @Mock
    BotQueue botQueue;

    @Mock
    SosaState sosaState;

    LichessChallengerBot lichessChallengerBot;

    @BeforeEach
    void before() {
        lichessChallengerBot = new LichessChallengerBot(client, sosaState, botQueue, List.of("bullet"));
    }

    @Test
    void youCannotChallengeYourselfTest() {
        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");

        User user = mock(UserProfileData.class);
        when(botQueue.pickBot()).thenReturn(user);
        when(user.id()).thenReturn("myId");

        Optional<Challenge> challengeOpt = lichessChallengerBot.challengeRandomBot();

        assertTrue(challengeOpt.isEmpty());

        verify(client, never()).challenge(any(), any());
    }
}
