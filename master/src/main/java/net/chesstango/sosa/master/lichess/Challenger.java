package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class Challenger {
    private final ChallengerBot challengerBot;
    private final ChallengerUser challengerUser;


    public Challenger(ChallengerBot challengerBot,
                      ChallengerUser challengerUser) {
        this.challengerBot = challengerBot;
        this.challengerUser = challengerUser;
    }

    public synchronized void challengeRandomBot() {
        Optional<Challenge> challengeOpt = challengerBot.challengeRandomBot();
        if (challengeOpt.isPresent()) {
            Challenge challenge = challengeOpt.get();
            log.info("[{}] Challenge sent: {}", challenge.id(), challenge);
        } else {
            log.warn("No challenge sent to any bot");
        }
    }


    private synchronized Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        return challengerUser.challengeUser(username, challengeType);
    }
}
