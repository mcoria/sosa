package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.lichess.LichessClient;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
@Slf4j
public class ChallengeWatchDogJob extends QuartzJobBean {

    private final LichessClient client;

    private final SosaState sosaState;

    public ChallengeWatchDogJob(LichessClient client, SosaState sosaState) {
        this.client = client;
        this.sosaState = sosaState;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String challengeId = context.getJobDetail().getJobDataMap().getString("challengeId");
        if (sosaState.isChallengePending(challengeId)) {
            log.info("[{}] Challenge watchdog triggered for challenge", challengeId);
            client.cancelChallenge(challengeId);
        }
    }
}
