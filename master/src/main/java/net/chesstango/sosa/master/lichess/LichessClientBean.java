package net.chesstango.sosa.master.lichess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessConnected;
import net.chesstango.sosa.master.events.LichessExceptionDetected;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessClientBean implements LichessClient {
    private final String botToken;
    private final ApplicationEventPublisher applicationEventPublisher;

    private volatile LichessClient imp;
    private volatile UserAuth myProfile;

    public LichessClientBean(@Value("${app.botToken}") String botToken,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.botToken = botToken;
        this.applicationEventPublisher = applicationEventPublisher;
        this.imp = null;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Connecting to Lichess...");

        try {
            ClientAuth clientAuth = Client.auth(botToken);

            this.imp = new LichessClientImp(clientAuth);

            this.myProfile = imp.getProfile();

            applicationEventPublisher.publishEvent(new LichessConnected(this));

        } catch (LichessException e) {
            log.error("Error connecting to Lichess", e);
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
        }
    }

    @Override
    public Stream<Event> streamEvents() {
        return imp.streamEvents();
    }


    @Override
    public UserAuth getProfile() {
        return myProfile;
    }

    @Override
    public Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        try {
            return imp.challenge(user, challengeBuilderConsumer);
        } catch (LichessException e) {
            applicationEventPublisher.publishEvent(new LichessExceptionDetected(this));
            throw e;
        }
    }

    @Override
    public void challengeAccept(String challengeId) {
        imp.challengeAccept(challengeId);
    }

    @Override
    public void challengeDecline(String challengeId) {
        imp.challengeDecline(challengeId);
    }

    @Override
    public void cancelChallenge(String challengeId) {
        imp.cancelChallenge(challengeId);
    }

    @Override
    public void gameMove(String gameId, String moveUci) {
        imp.gameMove(gameId, moveUci);
    }

    @Override
    public void gameResign(String gameId) {
        imp.gameResign(gameId);
    }

    @Override
    public void gameChat(String gameId, String message) {
        imp.gameChat(gameId, message);
    }

    @Override
    public void gameAbort(String gameId) {
        imp.gameAbort(gameId);
    }

    @Override
    public Stream<User> botsOnline() {
        return imp.botsOnline();
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        return imp.findUser(username);
    }

    @Override
    public Stream<GameInfo> meOngoingGames() {
        return imp.meOngoingGames();
    }
}
