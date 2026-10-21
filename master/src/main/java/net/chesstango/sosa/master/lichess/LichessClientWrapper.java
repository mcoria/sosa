package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
@Primary
public class LichessClientWrapper implements LichessClient {

    private final LichessClientImp lichessClientImp;

    private final CircuitBreakerFactory circuitBreakerFactory;

    public LichessClientWrapper(LichessClientImp lichessClientImp,
                                CircuitBreakerFactory circuitBreakerFactory) {
        this.lichessClientImp = lichessClientImp;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public UserAuth getProfile() {
        return circuitBreakerFactory.create("lichess").run(lichessClientImp::getProfile);
    }

    @Override
    public Stream<Event> streamEvents() {
        return circuitBreakerFactory.create("lichess").run(lichessClientImp::streamEvents);
    }

    @Override
    public Stream<User> botsOnline() {
        return circuitBreakerFactory.create("lichess").run(lichessClientImp::botsOnline);
    }

    @Override
    public Stream<GameInfo> meOngoingGames() {
        return circuitBreakerFactory.create("lichess").run(lichessClientImp::meOngoingGames);
    }

    @Override
    public Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        return circuitBreakerFactory.create("lichess").run(() -> lichessClientImp.challenge(user, challengeBuilderConsumer));
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        return circuitBreakerFactory.create("lichess").run(() -> lichessClientImp.findUser(username));
    }

    @Override
    public Optional<ChallengeInfo> getChallenge(String challengeId) {
        return circuitBreakerFactory.create("lichess").run(() -> lichessClientImp.getChallenge(challengeId));
    }

    @Override
    public boolean isOnline(User theBot) {
        return circuitBreakerFactory.create("lichess").run(() -> lichessClientImp.isOnline(theBot));
    }

    @Override
    public void challengeAccept(String challengeId) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.challengeAccept(challengeId);
            return null;
        });
    }

    @Override
    public void challengeDecline(String challengeId) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.challengeDecline(challengeId);
            return null;
        });
    }

    @Override
    public void cancelChallenge(String challengeId) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.cancelChallenge(challengeId);
            return null;
        });
    }

    @Override
    public void gameMove(String gameId, String moveUci) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.gameMove(gameId, moveUci);
            return null;
        });
    }

    @Override
    public void gameResign(String gameId) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.gameResign(gameId);
            return null;
        });
    }

    @Override
    public void gameChat(String gameId, String message) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.gameChat(gameId, message);
            return null;
        });
    }

    @Override
    public void gameAbort(String gameId) {
        circuitBreakerFactory.create("lichess").<Void>run(() -> {
            lichessClientImp.gameAbort(gameId);
            return null;
        });
    }
}
