package net.chesstango.sosa.master;

import chariot.model.ChallengeInfo;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class ChallengeExpire {
    private final LichessClient client;

    public ChallengeExpire(LichessClient client) {
        this.client = client;
    }

    public boolean expire(String challengeId) {
        Optional<ChallengeInfo> challengeInfoOpt = client.getChallenge(challengeId);
        if (challengeInfoOpt.isEmpty()) {
            log.warn("[{}] Challenge not found", challengeId);
            return true;
        }

        ChallengeInfo challengeInfo = challengeInfoOpt.get();

        String challengeStatus = challengeInfo.status().orElseGet(() -> "");

        log.warn("[{}] Challenge status: {}", challengeId, challengeStatus);

        if (challengeStatus.equals("created") || challengeStatus.equals("offline")) {
            client.cancelChallenge(challengeId);
            return false;
        }

        if (challengeStatus.equals("canceled") || challengeStatus.equals("declined") || challengeStatus.equals("accepted")) {
            return true;
        }

        log.warn("[{}] Unknown status.", challengeId);

        return false;
    }
}
