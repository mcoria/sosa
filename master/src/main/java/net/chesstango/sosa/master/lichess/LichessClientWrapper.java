package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
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
public class LichessClientWrapper implements LichessClient{

    private final LichessClientImp lichessClientImp;

    public LichessClientWrapper(LichessClientImp lichessClientImp) {
        this.lichessClientImp = lichessClientImp;
    }

    @Override
    public UserAuth getProfile() {
        return lichessClientImp.getProfile();
    }

    @Override
    public Stream<Event> streamEvents() {
        return lichessClientImp.streamEvents();
    }

    @Override
    public Stream<User> botsOnline() {
        return lichessClientImp.botsOnline();
    }

    @Override
    public Stream<GameInfo> meOngoingGames() {
        return lichessClientImp.meOngoingGames();
    }

    @Override
    public Challenge challenge(User user, Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer) {
        return lichessClientImp.challenge(user, challengeBuilderConsumer);
    }

    @Override
    public void challengeAccept(String challengeId) {
        lichessClientImp.challengeAccept(challengeId);
    }

    @Override
    public void challengeDecline(String challengeId) {
        lichessClientImp.challengeDecline(challengeId);
    }

    @Override
    public void cancelChallenge(String challengeId) {
        lichessClientImp.cancelChallenge(challengeId);
    }

    @Override
    public void gameMove(String gameId, String moveUci) {
        lichessClientImp.gameMove(gameId, moveUci);
    }

    @Override
    public void gameResign(String gameId) {
        lichessClientImp.gameResign(gameId);
    }

    @Override
    public void gameChat(String gameId, String message) {
        lichessClientImp.gameChat(gameId, message);
    }

    @Override
    public void gameAbort(String gameId) {
        lichessClientImp.gameAbort(gameId);
    }

    @Override
    public Optional<UserAuth> findUser(String username) {
        return lichessClientImp.findUser(username);
    }

    @Override
    public Optional<ChallengeInfo> getChallenge(String challengeId) {
        return lichessClientImp.getChallenge(challengeId);
    }

    @Override
    public boolean isOnline(User theBot) {
        return lichessClientImp.isOnline(theBot);
    }
}
