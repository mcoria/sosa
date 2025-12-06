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
    UserAuth getProfile();

    Stream<Event> streamEvents();

    Stream<User> botsOnline();

    Stream<GameInfo> meOngoingGames();

    Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer);

    void challengeAccept(String challengeId);

    void challengeDecline(String challengeId);

    void cancelChallenge(String challengeId);

    void gameMove(String gameId, String moveUci);

    void gameResign(String gameId);

    void gameChat(String gameId, String message);

    void gameAbort(String gameId);

    Optional<UserAuth> findUser(String username);
}
