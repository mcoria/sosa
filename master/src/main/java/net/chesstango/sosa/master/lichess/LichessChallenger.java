package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.ChallengeEvent;
import org.springframework.context.ApplicationEventPublisher;
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
    private final SosaState sosaState;
    private final ApplicationEventPublisher applicationEventPublisher;


    public LichessChallenger(LichessClient client, SosaState sosaState, ApplicationEventPublisher applicationEventPublisher) {
        this.lichessChallengerBot = new LichessChallengerBot(client);
        this.lichessChallengerUser = new LichessChallengerUser(client);
        this.sosaState = sosaState;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public synchronized void challengeRandom() {
        if (!sosaState.isBusy()) {
            log.info("Challenging random bot");
            Optional<Challenge> challengeOpt = challengeRandomBot();
            if (challengeOpt.isPresent()) {
                Challenge challenge = challengeOpt.get();
                log.info("[{}] Challenge sent: {}", challenge.id(), challengeOpt);
                applicationEventPublisher.publishEvent(new ChallengeEvent(this, ChallengeEvent.Type.CHALLENGE_CREATED, challenge.id()));
            } else {
                log.warn("Couldn't sent challenge");
            }
        }
    }

    private Optional<Challenge> challengeRandomBot() {
        return lichessChallengerBot.challengeRandomBot();
    }


    private Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        return lichessChallengerUser.challengeUser(username, challengeType);
    }
}
