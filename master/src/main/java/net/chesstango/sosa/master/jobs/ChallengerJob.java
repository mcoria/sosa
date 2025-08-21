package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Component
@Slf4j
public class ChallengerJob extends QuartzJobBean {


    @Override
    protected void executeInternal(JobExecutionContext context) {
        log.info("executeInternal ...");
    }

}
