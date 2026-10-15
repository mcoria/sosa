package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import org.jspecify.annotations.NonNull;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@PersistJobDataAfterExecution
@Component
@Slf4j
public class ChallengeBotJob extends QuartzJobBean {

    private final SosaState sosaState;

    public ChallengeBotJob(SosaState sosaState) {
        this.sosaState = sosaState;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        sosaState.challengeBot();
    }
}

