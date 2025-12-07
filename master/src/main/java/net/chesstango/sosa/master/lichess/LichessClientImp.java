package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessExceptionDetected;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessClientImp implements LichessClient {
    private final ClientAuth client;
    private final ApplicationEventPublisher applicationEventPublisher;

    public LichessClientImp(ClientAuth client,
                            ApplicationEventPublisher applicationEventPublisher) {
        this.client = client;
        this.applicationEventPublisher = applicationEventPublisher;
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
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error sending challenge to " + user.id());
        }
    }

    @Override
    public synchronized void challengeAccept(String challengeId) {
        One<Void> result = client.bot().acceptChallenge(challengeId);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error accepting challenge " + challengeId);
        }
    }

    @Override
    public synchronized void challengeDecline(String challengeId) {
        One<Void> result = client.bot()
                .declineChallenge(challengeId);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error declining challenge " + challengeId);
        }
    }

    @Override
    public synchronized void cancelChallenge(String challengeId) {
        One<Void> result = client.bot()
                .cancelChallenge(challengeId);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error cancelling challenge " + challengeId);
        }
    }

    @Override
    public synchronized void gameMove(String gameId, String moveUci) {
        One<Void> result = client.bot()
                .move(gameId, moveUci);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error sending move " + moveUci);
        }
    }

    @Override
    public synchronized void gameResign(String gameId) {
        One<Void> result = client.bot()
                .resign(gameId);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error resigning game " + gameId);
        }
    }

    @Override
    public synchronized void gameChat(String gameId, String message) {
        One<Void> result = client.bot()
                .chat(gameId, message);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error resigning game " + gameId);
        }
    }

    @Override
    public synchronized void gameAbort(String gameId) {
        One<Void> result = client.bot()
                .abort(gameId);
        if (result instanceof Fail<Void>) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error resigning game " + gameId);
        }
    }

    @Override
    public synchronized UserAuth getProfile() {
        One<UserAuth> userAuthOne = client.account()
                .profile();

        if (userAuthOne.isPresent()) {
            return userAuthOne.get();
        } else {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
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

        if (userAuthMany instanceof Fail) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error searching user " + username);
        }

        return userAuthMany.stream().findFirst();
    }


    @Override
    public synchronized Stream<GameInfo> meOngoingGames() {
        Many<GameInfo> gameInfoMany = client.games().ongoing();

        if (gameInfoMany instanceof Fail) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw new LichessException("Error getting ongoing games");
        }

        return client.games().ongoing().stream();
    }

}
