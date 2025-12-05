package net.chesstango.sosa.worker.lichess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.LichessConnected;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;
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

    private LichessClient imp;

    public LichessClientBean(@Value("${app.botToken}") String botToken,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.botToken = botToken;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Connecting to Lichess...");

        ClientAuth clientAuth = Client.auth(botToken);

        imp = new LichessClientImp(clientAuth);

        applicationEventPublisher.publishEvent(new LichessConnected(this));
    }

    @Override
    public Stream<Event> streamEvents() {
        return imp.streamEvents();
    }

    @Override
    public Stream<GameStateEvent> streamGameStateEvent(String gameId) {
        return imp.streamGameStateEvent(gameId);
    }

    @Override
    public Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        return imp.challenge(user, challengeBuilderConsumer);
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
    public Map<StatsPerfType, StatsPerf> getRatings() {
        return imp.getRatings();
    }

    @Override
    public int getRating(StatsPerfType type) {
        return imp.getRating(type);
    }

    @Override
    public boolean isMe(UserInfo theUser) {
        return imp.isMe(theUser);
    }

    @Override
    public Many<User> botsOnline() {
        return imp.botsOnline();
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        return imp.findUser(username);
    }

    @Override
    public Many<GameInfo> meOngoingGames() {
        return imp.meOngoingGames();
    }
}
