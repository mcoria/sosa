package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class LichessClientImp implements LichessClient {
    private final ClientAuth client;

    public LichessClientImp(ClientAuth client) {
        this.client = client;
    }

    @Override
    public synchronized Stream<Event> streamEvents() {
        return client.bot().connect().stream();
    }

    @Override
    public synchronized Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        return client.bot().connectToGame(gameId).stream();
    }

    @Override
    public synchronized Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        One<Challenge> optChallenge = client.bot()
                .challenge(user.id(), challengeBuilderConsumer);

        if(optChallenge.isPresent()){
            return optChallenge.get();
        } else {
            throw new RuntimeException("Error sending challenge");
        }
    }

    @Override
    public synchronized void challengeAccept(String challengeId) {
        client.bot().acceptChallenge(challengeId);
    }

    @Override
    public synchronized void challengeDecline(String challengeId) {
        client.bot().declineChallenge(challengeId);
    }

    @Override
    public synchronized void cancelChallenge(String challengeId) {
        client.bot().cancelChallenge(challengeId);
    }

    @Override
    public synchronized void gameMove(String gameId, String moveUci) {
        client.bot().move(gameId, moveUci);
    }

    @Override
    public synchronized void gameResign(String gameId) {
        client.bot().resign(gameId);
    }

    @Override
    public synchronized void gameChat(String gameId, String message) {
        client.bot().chat(gameId, message);
    }

    @Override
    public synchronized void gameAbort(String gameId) {
        client.bot().abort(gameId);
    }

    @Override
    public synchronized Map<StatsPerfType, StatsPerf> getRatings() {
        return client.account()
                .profile()
                .get()
                .ratings();
    }

    @Override
    public synchronized int getRating(StatsPerfType type) {
        Map<StatsPerfType, StatsPerf> rating = client.account()
                .profile()
                .get()
                .ratings();
        StatsPerf stats = rating.get(type);
        if (stats instanceof StatsPerf.StatsPerfGame statsPerfGame) {
            return statsPerfGame.rating();
        }
        throw new RuntimeException("Rating not found");
    }

    @Override
    public synchronized boolean isMe(UserInfo theUser) {
        return client.account().profile().get().id().equals(theUser.id());
    }

    @Override
    public synchronized Many<User> botsOnline() {
        return client.bot().botsOnline();
    }

    @Override
    public synchronized Optional<UserAuth> findUser(String username) {
        Many<UserAuth> users = client.users().byIds(List.of(username));
        return users.stream().findFirst();
    }


    @Override
    public synchronized Many<GameInfo> meOngoingGames() {
        return client.games().ongoing();
    }
}
