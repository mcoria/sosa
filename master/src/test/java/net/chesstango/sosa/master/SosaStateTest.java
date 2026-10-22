package net.chesstango.sosa.master;

import chariot.model.*;
import net.chesstango.sosa.master.lichess.Challenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class SosaStateTest {

    @Mock
    private Challenger challenger;

    @Mock
    private ChallengeExpire challengeExpire;

    private SosaState sosaState;

    @BeforeEach
    void setUp() {
        sosaState = new SosaState(challenger, challengeExpire);
    }

    @Test
    @DisplayName("isChallengeAcceptable for human when workers available")
    void isChallengeAcceptable_human_withAvailableWorkers() {
        assertThat(sosaState.isChallengeAcceptable(true)).isFalse();

        sosaState.addAvailableWorker("w1");
        assertThat(sosaState.isChallengeAcceptable(true)).isTrue();

        sosaState.addChallenge("c1");
        assertThat(sosaState.isChallengeAcceptable(true)).isFalse();

        sosaState.addAvailableWorker("w2");
        assertThat(sosaState.isChallengeAcceptable(true)).isTrue();
    }

    @Test
    @DisplayName("isChallengeAcceptable for bot when workers available")
    void isChallengeAcceptable_bot_withAvailableWorkers() {
        assertThat(sosaState.isChallengeAcceptable(false)).isFalse();

        sosaState.addAvailableWorker("w1");
        assertThat(sosaState.isChallengeAcceptable(false)).isFalse();

        sosaState.addAvailableWorker("w2");
        assertThat(sosaState.isChallengeAcceptable(false)).isTrue();

        sosaState.addChallenge("c1");
        assertThat(sosaState.isChallengeAcceptable(false)).isFalse();

        sosaState.addAvailableWorker("w3");
        assertThat(sosaState.isChallengeAcceptable(false)).isTrue();
    }

    @Test
    @DisplayName("Worker management: add, remove, and availability check")
    void workerManagement() {
        assertThat(sosaState.thereIsAvailableWorker()).isFalse();

        sosaState.addAvailableWorker("w1");
        assertThat(sosaState.thereIsAvailableWorker()).isTrue();

        // Updating existing worker
        sosaState.addAvailableWorker("w1");
        assertThat(sosaState.thereIsAvailableWorker()).isTrue();

        sosaState.removeAvailableWorker("w1");
        assertThat(sosaState.thereIsAvailableWorker()).isFalse();
    }

    @Test
    @DisplayName("Game lifecycle: add game removes challenge and tracks game")
    void gameLifecycle() {
        sosaState.addChallenge("g1");
        assertThat(sosaState.isInProgressGame("g1")).isFalse();

        sosaState.addGame("g1");
        assertThat(sosaState.isInProgressGame("g1")).isTrue();

        sosaState.removeGame("g1");
        assertThat(sosaState.isInProgressGame("g1")).isFalse();
    }

    @Test
    @DisplayName("challengeBot triggers challenge when workers available for bot")
    void challengeBot_whenWorkersAvailable() {
        UserAuth userAuth = mock(UserProfileData.class);
        sosaState.setMyProfile(userAuth);

        sosaState.addAvailableWorker("w1");
        sosaState.addAvailableWorker("w2");

        ChallengeInfo challenge = mock(ChallengeInfo.class);
        when(challenge.id()).thenReturn("botChallenge1");
        when(challenger.challengeRandomBot(userAuth)).thenReturn(Optional.of(challenge));

        sosaState.challengeBot();

        verify(challenger).challengeRandomBot(userAuth);
        // Worker count 2, in-progress challenges 1 -> bot challenge acceptable is now false
        assertThat(sosaState.isChallengeAcceptable(false)).isFalse();
    }

    @Test
    @DisplayName("challengeBot does nothing when not enough workers")
    void challengeBot_whenNotEnoughWorkers() {
        sosaState.addAvailableWorker("w1");

        sosaState.challengeBot();

        verifyNoInteractions(challenger);
    }

    @Test
    @DisplayName("getMyRating returns rating for valid StatsPerfGame")
    void getMyRating_valid() {
        UserAuth userAuth = mock(UserProfileData.class);
        StatsPerf.StatsPerfGame statsPerfGame = mock(StatsPerf.StatsPerfGame.class);
        when(statsPerfGame.rating()).thenReturn(1650);

        Map<StatsPerfType, StatsPerf> ratingsMap = Map.of(StatsPerfType.blitz, statsPerfGame);
        when(userAuth.ratings()).thenReturn(ratingsMap);

        sosaState.setMyProfile(userAuth);

        int rating = sosaState.getMyRating(StatsPerfType.blitz);
        assertThat(rating).isEqualTo(1650);
    }

    @Test
    @DisplayName("getMyRating throws exception when rating is not StatsPerfGame or missing")
    void getMyRating_notFound() {
        UserAuth userAuth = mock(UserProfileData.class);

        Map<StatsPerfType, StatsPerf> ratingsMap = Map.of();
        when(userAuth.ratings()).thenReturn(ratingsMap);

        sosaState.setMyProfile(userAuth);

        assertThatThrownBy(() -> sosaState.getMyRating(StatsPerfType.blitz))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Rating not found");
    }
}
