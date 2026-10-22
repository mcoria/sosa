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
    @DisplayName("getStatus returns empty collections when state is unpopulated")
    void getStatus_empty() {
        SosaStatus status = sosaState.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.availableWorkers()).isEmpty();
        assertThat(status.inProgressChallenges()).isEmpty();
        assertThat(status.inProgressGames()).isEmpty();
    }

    @Test
    @DisplayName("getStatus returns snapshot of current workers, challenges, and games")
    void getStatus_populated() {
        sosaState.addAvailableWorker("w1");
        sosaState.addAvailableWorker("w2");
        sosaState.addChallenge("c1");
        sosaState.addGame("g1");

        SosaStatus status = sosaState.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.availableWorkers())
                .extracting(Worker::getWorkerId)
                .containsExactlyInAnyOrder("w1", "w2");
        assertThat(status.inProgressChallenges())
                .extracting(Challenge::getChallengeId)
                .containsExactly("c1");
        assertThat(status.inProgressGames())
                .containsExactly("g1");
    }

    @Test
    @DisplayName("getStatus returns unmodifiable collections protecting internal state")
    void getStatus_unmodifiableCollections() {
        sosaState.addAvailableWorker("w1");
        sosaState.addChallenge("c1");
        sosaState.addGame("g1");

        SosaStatus status = sosaState.getStatus();

        assertThatThrownBy(() -> status.availableWorkers().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> status.inProgressChallenges().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> status.inProgressGames().clear())
                .isInstanceOf(UnsupportedOperationException.class);
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
