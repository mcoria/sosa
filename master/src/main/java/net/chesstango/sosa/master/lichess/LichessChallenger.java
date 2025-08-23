package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.BusyEvent;
import net.chesstango.sosa.master.events.OnGoingChallengesEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessChallenger implements ApplicationListener<SosaEvent> {
    private final LichessChallengerBot lichessChallengerBot;
    private final LichessChallengerUser lichessChallengerUser;

    private final AtomicBoolean isBusy = new AtomicBoolean(false);
    private final AtomicBoolean onGoingChallenges = new AtomicBoolean(false);

    public LichessChallenger(LichessClient client) {
        this.lichessChallengerBot = new LichessChallengerBot(client);
        this.lichessChallengerUser = new LichessChallengerUser(client);
    }

    public void challengeRandom() {
        if (!isBusy.get() && !onGoingChallenges.get()) {
            log.info("Challenging random bot");
            Optional<Challenge> challenge = challengeRandomBot();
            if (challenge.isPresent()) {
                log.info("[{}] Challenge sent: {}", challenge.get().id(), challenge);
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

    @Override
    public void onApplicationEvent(SosaEvent event) {
        if (event instanceof BusyEvent busyEvent) {
            isBusy.set(busyEvent.isBusy());
        } else if (event instanceof OnGoingChallengesEvent onGoingChallengesEvent) {
            onGoingChallenges.set(onGoingChallengesEvent.isOnGoing());
        }
    }
}
