package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
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
                             LichessChallengerUser lichessChallengerUser) {
        this.lichessChallengerBot = lichessChallengerBot;
        this.lichessChallengerUser = lichessChallengerUser;
    }

    @Async
    public synchronized void challengeRandomBot() {
        Optional<Challenge> challengeOpt = lichessChallengerBot.challengeRandomBot();
        if (challengeOpt.isPresent()) {
            Challenge challenge = challengeOpt.get();
            log.info("[{}] Challenge sent: {}", challenge.id(), challenge);
        } else {
            log.warn("Couldn't challenge random bot");
        }
    }


    private synchronized Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        return lichessChallengerUser.challengeUser(username, challengeType);
    }
}
