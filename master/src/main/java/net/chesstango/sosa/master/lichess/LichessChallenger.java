package net.chesstango.sosa.master.lichess;

import chariot.model.Challenge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class LichessChallenger {

    private final LichessChallengerBot lichessChallengerBot;
    private final LichessChallengerUser lichessChallengerUser;

    public LichessChallenger(LichessClient client) {
        lichessChallengerBot = new LichessChallengerBot(client);
        lichessChallengerUser = new LichessChallengerUser(client);
    }

    @Async("ioBoundExecutor")
    public CompletableFuture<Void> doWorkAsync(Consumer<Challenge> fnChallengeConsumer) {
        log.info("Challenging random bot");

        Optional<Challenge> challenge = challengeRandomBot();

        if (challenge.isPresent()) {
            fnChallengeConsumer.accept(challenge.get());
        } else {
            log.warn("No challenges found");
        }

        return CompletableFuture.completedFuture(null);
    }

    private Optional<Challenge> challengeRandomBot() {
        return lichessChallengerBot.challengeRandomBot();
    }


    private Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        return lichessChallengerUser.challengeUser(username, challengeType);
    }
}
