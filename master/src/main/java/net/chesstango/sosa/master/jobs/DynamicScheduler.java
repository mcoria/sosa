package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import static net.chesstango.sosa.master.lichess.LichessGame.EXPIRED_THRESHOLD;

/**
 * @author Mauricio Coria
 */
@Service
@Slf4j
public class DynamicScheduler {
    public static final int EXPIRED_TOLERANCE = 5;

    private final Scheduler scheduler;

    public DynamicScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void scheduleGameWatchDog(String gameId) {
        try {
            JobDetail job = JobBuilder.newJob(GameWatchDogJob.class)
                    .withIdentity(String.format("gameWatchDogJob-%s", gameId))
                    .usingJobData("gameId", gameId)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(String.format("gameWatchDogTrigger-%s", gameId))
                    .startAt(DateBuilder.futureDate(EXPIRED_THRESHOLD + EXPIRED_TOLERANCE, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("SchedulerException:", e);
            throw new RuntimeException(e);
        }
    }
}
