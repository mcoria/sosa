package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public interface LichessClient {
    Stream<Event> streamEvents();

    Stream<User> botsOnline();

    Stream<GameInfo> meOngoingGames();

    UserAuth getProfile();

    Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer);

    Optional<UserAuth> findUser(String username);

    Optional<ChallengeInfo> getChallenge(String challengeId);

    boolean isOnline(User theBot);

    void challengeAccept(String challengeId);

    void challengeDecline(String challengeId);

    void cancelChallenge(String challengeId);

    void gameMove(String gameId, String moveUci);

    void gameResign(String gameId);

    void gameChat(String gameId, String message);

    void gameAbort(String gameId);
}
