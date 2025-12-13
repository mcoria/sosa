package net.chesstango.sosa.master.jobs;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.errors.RetryIn;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class DynamicScheduler {
    private final Scheduler scheduler;

    @Value("${app.gameWatchDog}")
    @Setter
    private Boolean gameWatchDogEnabled;

    @Value("${app.gameExpire}")
    @Setter
    private int GAME_EXPIRE = 30;

    @Value("${app.challengeExpire}")
    @Setter
    private int CHALLENGE_EXPIRE = 15;


    public DynamicScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    @EventListener
    public void onLichessTooManyGamesPlayed(LichessTooManyGamesPlayed lichessTooManyGamesPlayed) {
        log.info("Scheduling Expired Timer Event for CHALLENGES");
        try {
            RetryIn retryIn = lichessTooManyGamesPlayed.getRetryIn();
            JobDetail job = JobBuilder.newJob(PublishExpiredTimerEvent.class)
                    .withIdentity("PublishTooManyExpiredTimerEvent_Game")
                    .usingJobData("expirationType", "GAMES")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("PublishTooManyExpiredTimerEventTrigger_Game")
                    .startAt(DateBuilder.futureDate((int) retryIn.getSeconds(), DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0) // Explicitly set repeat count to 0 (runs once)
                            .withIntervalInSeconds(0))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }

    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequestsSent() {
        log.info("Scheduling Expired Timer Event for lichess REQUESTS");
        try {
            JobDetail job = JobBuilder.newJob(PublishExpiredTimerEvent.class)
                    .withIdentity("PublishTooManyExpiredTimerEvent_Requests")
                    .usingJobData("expirationType", "REQUESTS")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("PublishTooManyExpiredTimerEventTrigger_Requests")
                    .startAt(DateBuilder.futureDate(90, DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0) // Explicitly set repeat count to 0 (runs once)
                            .withIntervalInSeconds(0))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }

}
