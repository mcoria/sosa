package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class ChallengerScheduler {
    private final Scheduler scheduler;

    public ChallengerScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /*
    public void scheduleOneOff() {
        try {
            JobDetail job = JobBuilder.newJob(ChallengerJob.class)
                    .withIdentity("challengerJob")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("oneOffTrigger")
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }*/
}
