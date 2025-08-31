package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import net.chesstango.sosa.master.events.LichessConnected;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Service
public class LichessClientBean implements LichessClient {

    private final ApplicationEventPublisher applicationEventPublisher;

    private volatile LichessClient imp;

    public LichessClientBean(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.imp = new LichessClientNoOp();
    }

    public void setImp(LichessClient lichessClient) {
        this.imp = lichessClient;
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
}
