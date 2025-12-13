package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.errors.RetryIn;
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
    public static final int TOO_MANY_REQUESTS = 429;

    private final ClientAuth client;
    private final LichessErrorParser lichessErrorParser;
    private final ApplicationEventPublisher applicationEventPublisher;


    public LichessClientImp(ClientAuth client,
                            LichessErrorParser lichessErrorParser,
                            ApplicationEventPublisher applicationEventPublisher) {
        this.client = client;
        this.lichessErrorParser = lichessErrorParser;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public synchronized UserAuth getProfile() {
        One<UserAuth> userAuthOne = client.account()
                .profile();

        if (userAuthOne instanceof Fail<UserAuth> fail) {
            if (fail.status() == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException("Error getting profile");
        }

        return userAuthOne.get();
    }

    @Override
    public synchronized Stream<Event> streamEvents() {
        return client.bot().connect().stream();
    }

    @Override
    public synchronized Optional<Challenge> challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        One<Challenge> challengeOne = client.bot()
                .challenge(user.id(), challengeBuilderConsumer);

        if (challengeOne.isPresent()) {
            log.info("Challenging {} succeeded", user.id());
            return Optional.of(challengeOne.get());
        }

        if (challengeOne instanceof Fail<Challenge>(int status, Err info)) {
            log.warn("Challenging {} failed: {}", user.id(), info.toString());
            if (status == TOO_MANY_REQUESTS) {
                Object erroPayload = lichessErrorParser.parse(info.message());
                if (erroPayload instanceof RetryIn retryIn) {
                    applicationEventPublisher.publishEvent(new LichessTooManyGamesPlayed(this, retryIn));
                } else {
                    applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
                }
            }
            return Optional.empty();
        }

        throw new RuntimeException("Unexpected challenge result");
    }

    @Override
    public synchronized void challengeAccept(String challengeId) {
        One<Void> result = client.bot().acceptChallenge(challengeId);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Accepting challenge %s failed: %s", challengeId, info.message()));
        }
    }

    @Override
    public synchronized void challengeDecline(String challengeId) {
        One<Void> result = client.bot()
                .declineChallenge(challengeId);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Declining challenge %s failed: %s", challengeId, info.message()));
        }
    }

    @Override
    public synchronized void cancelChallenge(String challengeId) {
        One<Void> result = client.bot()
                .cancelChallenge(challengeId);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Cancelling challenge %s failed: %s", challengeId, info.message()));
        }
    }

    @Override
    public synchronized void gameMove(String gameId, String moveUci) {
        One<Void> result = client.bot()
                .move(gameId, moveUci);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Moving %s in game %s failed: %s", moveUci, gameId, info.message()));
        }
    }

    @Override
    public synchronized void gameResign(String gameId) {
        One<Void> result = client.bot()
                .resign(gameId);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Resigning game %s failed: %s", gameId, info.message()));
        }
    }

    @Override
    public synchronized void gameChat(String gameId, String message) {
        One<Void> result = client.bot()
                .chat(gameId, message);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Resigning game %s failed: %s", gameId, info.message()));
        }
    }

    @Override
    public synchronized void gameAbort(String gameId) {
        One<Void> result = client.bot()
                .abort(gameId);
        if (result instanceof Fail<Void>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Aborting game %s failed: %s", gameId, info.message()));
        }
    }

    @Override
    public synchronized Stream<User> botsOnline() {
        return client.bot().botsOnline().stream();
    }

    @Override
    public synchronized Optional<UserAuth> findUser(String username) {
        Many<UserAuth> userAuthMany = client.users().byIds(List.of(username));

        if (userAuthMany instanceof Fail<UserAuth>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Finding user %s failed: %s", username, info.message()));
        }

        return userAuthMany.stream().findFirst();
    }


    @Override
    public synchronized Stream<GameInfo> meOngoingGames() {
        Many<GameInfo> gameInfoMany = client.games().ongoing();

        if (gameInfoMany instanceof Fail<GameInfo>(int status, Err info)) {
            if (status == TOO_MANY_REQUESTS) {
                applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(this));
            }
            throw new RuntimeException(String.format("Getting ongoing failed: %s", info.message()));
        }

        return gameInfoMany.stream();
    }

}
