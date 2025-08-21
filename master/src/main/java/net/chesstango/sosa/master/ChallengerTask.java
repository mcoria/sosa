package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessChallenger;
import net.chesstango.sosa.master.lichess.LichessClientBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class ChallengerTask {

    private final LichessChallenger lichessChallenger;

    public ChallengerTask(LichessClientBean lichessClientBean) {
        this.lichessChallenger = new LichessChallenger(lichessClientBean);
    }

    @Async("ioBoundExecutor")
    public CompletableFuture<Void> doWorkAsync() {
        log.info("Challenging random bot");

        lichessChallenger.challengeRandomBot();

        return CompletableFuture.completedFuture(null);
    }
}
