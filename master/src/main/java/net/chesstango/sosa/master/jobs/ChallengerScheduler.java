package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChallengerScheduler {
    private final Scheduler scheduler;


    public ChallengerScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleOneOff() {
        try {
            JobDetail job = JobBuilder.newJob(ChallengerJob.class)
                    .withIdentity("oneOffJob")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("oneOffTrigger")
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}
