package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessChallenger {
    private final LichessChallengerBot lichessChallengerBot;
    private final LichessChallengerUser lichessChallengerUser;


    public LichessChallenger(LichessChallengerBot lichessChallengerBot,
                             LichessClient client) {
        this.lichessChallengerBot = lichessChallengerBot;
        this.lichessChallengerUser = new LichessChallengerUser(client);
    }

    @Async
    public synchronized void challengeRandom() {
        log.info("Challenging random bot");
        Optional<Challenge> challengeOpt = challengeRandomBot();
        if (challengeOpt.isPresent()) {
            Challenge challenge = challengeOpt.get();
            log.info("[{}] Challenge sent: {}", challenge.id(), challenge);
        } else {
            log.warn("Couldn't challenge random bot");
        }
    }

    private Optional<Challenge> challengeRandomBot() {
        return lichessChallengerBot.challengeRandomBot();
    }


    private Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        return lichessChallengerUser.challengeUser(username, challengeType);
    }
}
