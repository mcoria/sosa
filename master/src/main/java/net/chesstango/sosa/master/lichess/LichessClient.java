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
public interface LichessClient {
    Stream<Event> streamEvents();

    Stream<GameStateEvent> streamGameStateEvent(String gameId);

    Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer);

    void challengeAccept(String challengeId);

    void challengeDecline(String challengeId);

    void gameMove(String gameId, String moveUci);

    void gameResign(String gameId);

    void gameChat(String gameId, String message);

    void gameAbort(String gameId);

    Map<StatsPerfType, StatsPerf> getRatings();

    int getRating(StatsPerfType type);

    boolean isMe(UserInfo theUser);

    Many<User> botsOnline();

    Optional<UserAuth> findUser(String username);
}
