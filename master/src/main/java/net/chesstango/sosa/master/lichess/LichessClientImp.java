package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessApiCallFailed;
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
    public synchronized UserAuth getProfile() {
        One<UserAuth> userAuthOne = client.account()
                .profile();

        if (userAuthOne instanceof Fail<UserAuth> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException("Error getting profile");
        }

        return userAuthOne.get();
    }

    @Override
    public synchronized Stream<Event> streamEvents() {
        return client.bot().connect().stream();
    }

    @Override
    public synchronized Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        One<Challenge> challengeOne = client.bot()
                .challenge(user.id(), challengeBuilderConsumer);

        if (challengeOne instanceof Fail<Challenge> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Challenging %s failed", user.id()));
        }

        return challengeOne.get();
    }

    @Override
    public synchronized void challengeAccept(String challengeId) {
        One<Void> result = client.bot().acceptChallenge(challengeId);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Accepting challenge %s failed", challengeId));
        }
    }

    @Override
    public synchronized void challengeDecline(String challengeId) {
        One<Void> result = client.bot()
                .declineChallenge(challengeId);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Declining challenge %s failed", challengeId));
        }
    }

    @Override
    public synchronized void cancelChallenge(String challengeId) {
        One<Void> result = client.bot()
                .cancelChallenge(challengeId);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Cancelling challenge %s failed", challengeId));
        }
    }

    @Override
    public synchronized void gameMove(String gameId, String moveUci) {
        One<Void> result = client.bot()
                .move(gameId, moveUci);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Moving %s in game %s failed ", moveUci, gameId));
        }
    }

    @Override
    public synchronized void gameResign(String gameId) {
        One<Void> result = client.bot()
                .resign(gameId);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Resigning game %s failed", gameId));
        }
    }

    @Override
    public synchronized void gameChat(String gameId, String message) {
        One<Void> result = client.bot()
                .chat(gameId, message);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Resigning game %s failed", gameId));
        }
    }

    @Override
    public synchronized void gameAbort(String gameId) {
        One<Void> result = client.bot()
                .abort(gameId);
        if (result instanceof Fail<Void> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Aborting game %s failed", gameId));
        }
    }

    @Override
    public synchronized Stream<User> botsOnline() {
        return client.bot().botsOnline().stream();
    }

    @Override
    public synchronized Optional<UserAuth> findUser(String username) {
        Many<UserAuth> userAuthMany = client.users().byIds(List.of(username));

        if (userAuthMany instanceof Fail<UserAuth> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException(String.format("Finding user %s failed", username));
        }

        return userAuthMany.stream().findFirst();
    }


    @Override
    public synchronized Stream<GameInfo> meOngoingGames() {
        Many<GameInfo> gameInfoMany = client.games().ongoing();

        if (gameInfoMany instanceof Fail<GameInfo> fail) {
            applicationEventPublisher.publishEvent(new LichessApiCallFailed(this, fail.toString()));
            throw new RuntimeException("Getting ongoing failed");
        }

        return client.games().ongoing().stream();
    }

}
