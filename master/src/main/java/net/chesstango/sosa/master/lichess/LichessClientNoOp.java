package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class LichessClientNoOp implements LichessClient {
    @Override
    public Stream<Event> streamEvents() {
        throw new RuntimeException("No implementation");
    }

    @Override
    public Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void challengeAccept(String challengeId) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void challengeDecline(String challengeId) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void gameMove(String gameId, String moveUci) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void gameResign(String gameId) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void gameChat(String gameId, String message) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public void gameAbort(String gameId) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public Map<StatsPerfType, StatsPerf> getRatings() {
        throw new RuntimeException("No implementation");
    }

    @Override
    public int getRating(StatsPerfType type) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public boolean isMe(UserInfo theUser) {
        throw new RuntimeException("No implementation");
    }

    @Override
    public Many<User> botsOnline() {
        throw new RuntimeException("No implementation");
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        throw new RuntimeException("No implementation");
    }
}
