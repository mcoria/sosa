package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessChallenger;
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
public class ChallengeJob extends QuartzJobBean {

    private final LichessChallenger lichessChallenger;

    public ChallengeJob(LichessChallenger lichessChallenger) {
        this.lichessChallenger = lichessChallenger;
    }


    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            lichessChallenger.challengeRandom();
        } catch (Exception e) {
            log.error("Error executing ChallengeJob", e);
        }
    }

}
