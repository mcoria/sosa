package net.chesstango.sosa.master.lichess;

import chariot.model.*;
import net.chesstango.sosa.master.SosaState;
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
public class LichessChallengeHandlerTest {

    LichessChallengeHandler lichessChallengeHandler;

    @Mock
    LichessClient client;

    @Mock
    SosaState sosaState;

    @BeforeEach
    void before() {
        lichessChallengeHandler = new LichessChallengeHandler(client, sosaState);
    }

    /**
     * Si yo creo un challenge, no respondemos nada
     *
     * Esto previene la situacion la cual:
     * 1) Yo creo un challenge
     * 2) El bot oponente declina
     * 3) Yo declino / acepto el challenge. Pero dado que se encuentra declinado, la respuesta es 404
     *
     */
    @Test
    void ignoreMyChallengesTest() {
        Event.ChallengeCreatedEvent challengeCreatedEvent = mock(Event.ChallengeCreatedEvent.class);
        ChallengeInfo challengeInfo = mock(ChallengeInfo.class);
        ChallengeInfo.FromTo fromToPlayers = mock(ChallengeInfo.FromTo.class);
        ChallengeInfo.Player player = mock(ChallengeInfo.Player.class);
        IdNameTitle user = mock(IdNameTitle.class);

        when(challengeCreatedEvent.id()).thenReturn("challengeId");
        when(challengeCreatedEvent.challenge()).thenReturn(challengeInfo);
        when(challengeInfo.players()).thenReturn(fromToPlayers);
        when(fromToPlayers.challenger()).thenReturn(player);
        when(player.user()).thenReturn(user);
        when(user.id()).thenReturn("myId");

        /**
         * Sosa setup
         */
        UserAuth userAuth = mock(UserProfileData.class);
        when(sosaState.getMyProfile()).thenReturn(userAuth);
        when(userAuth.id()).thenReturn("myId");


        lichessChallengeHandler.challengeCreated(challengeCreatedEvent);

        /**
         * No respondemos nada
         */
        verifyNoInteractions(client);
    }
}
