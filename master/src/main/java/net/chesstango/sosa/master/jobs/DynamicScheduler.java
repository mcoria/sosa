package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class DynamicScheduler {
    private final Scheduler scheduler;

    public DynamicScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleGameWatchDog(String gameId) {
        try {
            JobDetail job = JobBuilder.newJob(GameWatchDogJob.class)
                    .withIdentity("gameWatchDogJob")
                    .usingJobData("gameId", gameId)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("gameWatchDogTrigger")
                    .startAt(DateBuilder.futureDate(15, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}
