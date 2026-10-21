package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    public LichessClientWrapper(LichessClientImp lichessClientImp,
                                ApplicationEventPublisher applicationEventPublisher) {
        this.lichessClientImp = lichessClientImp;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public UserAuth getProfile() {
        try {
            return lichessClientImp.getProfile();
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        try {
            return lichessClientImp.challenge(user, challengeBuilderConsumer);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Stream<Event> streamEvents() {
        try {
            return lichessClientImp.streamEvents();
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Stream<User> botsOnline() {
        try {
            return lichessClientImp.botsOnline();
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Stream<GameInfo> meOngoingGames() {
        try {
            return lichessClientImp.meOngoingGames();
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        try {
            return lichessClientImp.findUser(username);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public Optional<ChallengeInfo> getChallenge(String challengeId) {
        try {
            return lichessClientImp.getChallenge(challengeId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public boolean isOnline(User theBot) {
        try {
            return lichessClientImp.isOnline(theBot);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void challengeAccept(String challengeId) {
        try {
            lichessClientImp.challengeAccept(challengeId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void challengeDecline(String challengeId) {
        try {
            lichessClientImp.challengeDecline(challengeId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void cancelChallenge(String challengeId) {
        try {
            lichessClientImp.cancelChallenge(challengeId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void gameMove(String gameId, String moveUci) {
        try {
            lichessClientImp.gameMove(gameId, moveUci);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void gameResign(String gameId) {
        try {
            lichessClientImp.gameResign(gameId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void gameChat(String gameId, String message) {
        try {
            lichessClientImp.gameChat(gameId, message);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }

    @Override
    public void gameAbort(String gameId) {
        try {
            lichessClientImp.gameAbort(gameId);
        } catch (TooManyRequestsException e) {
            applicationEventPublisher.publishEvent(new LichessTooManyRequestsSent(e));
            throw e;
        }
    }
}
