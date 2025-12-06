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
    public synchronized Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        One<Challenge> challengeOne = client.bot()
                .challenge(user.id(), challengeBuilderConsumer);

        if (challengeOne.isPresent()) {
            return challengeOne.get();
        } else {
            throw new LichessException("Error sending challenge to " + user.id());
        }
    }

    @Override
    public synchronized void challengeAccept(String challengeId) {
        One<Void> result = client.bot().acceptChallenge(challengeId);
        if( result instanceof Fail<Void>){
            throw new LichessException("Error accepting challenge " + challengeId);
        }
    }

    @Override
    public synchronized void challengeDecline(String challengeId) {
        One<Void> result = client.bot().declineChallenge(challengeId);
    }

    @Override
    public synchronized void cancelChallenge(String challengeId) {
        One<Void> result = client.bot().cancelChallenge(challengeId);
    }

    @Override
    public synchronized void gameMove(String gameId, String moveUci) {
        One<Void> result = client.bot().move(gameId, moveUci);
    }

    @Override
    public synchronized void gameResign(String gameId) {
        One<Void> result = client.bot().resign(gameId);
    }

    @Override
    public synchronized void gameChat(String gameId, String message) {
        One<Void> result = client.bot().chat(gameId, message);
    }

    @Override
    public synchronized void gameAbort(String gameId) {
        One<Void> result = client.bot().abort(gameId);
    }

    @Override
    public synchronized UserAuth getProfile() {
        One<UserAuth> userAuthOne =  client.account()
                .profile();

        if (userAuthOne.isPresent()) {
            return userAuthOne.get();
        } else {
            throw new LichessException("Error getting profile");
        }
    }

    @Override
    public synchronized Stream<User> botsOnline() {
        return client.bot().botsOnline().stream();
    }

    @Override
    public synchronized Optional<UserAuth> findUser(String username) {
        Many<UserAuth> userAuthMany = client.users().byIds(List.of(username));

        if(userAuthMany instanceof Fail){
            throw new LichessException("Error searching user " + username);
        }

        return userAuthMany.stream().findFirst();
    }


    @Override
    public synchronized Stream<GameInfo> meOngoingGames() {
        Many<GameInfo> gameInfoMany = client.games().ongoing();

        if(gameInfoMany instanceof Fail){
            throw new LichessException("Error getting ongoing games");
        }

        return client.games().ongoing().stream();
    }

}
