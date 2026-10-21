package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.RetryIn;
import org.quartz.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class DynamicScheduler {
    private final Scheduler scheduler;

    private final JobDetail publishExpiredTimerEventRequestsJob;

    private final JobDetail publishExpiredTimerEventGamesJob;


    public DynamicScheduler(Scheduler scheduler,
                            JobDetail publishExpiredTimerEventRequestsJob,
                            JobDetail publishExpiredTimerEventGamesJob) {
        this.scheduler = scheduler;
        this.publishExpiredTimerEventRequestsJob = publishExpiredTimerEventRequestsJob;
        this.publishExpiredTimerEventGamesJob = publishExpiredTimerEventGamesJob;
    }

    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequestsSent() {
        log.info("Scheduling Expired Timer Event for lichess REQUESTS");
        try {

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(publishExpiredTimerEventRequestsJob)
                    .withIdentity("PublishTooManyExpiredTimerEventTrigger_Requests-" + UUID.randomUUID())
                    .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.HOUR))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0) // Explicitly set repeat count to 0 (runs once)
                            .withIntervalInSeconds(0))
                    .build();


            scheduler.scheduleJob(trigger);


        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }


    @EventListener
    public void onLichessTooManyGamesPlayed(LichessTooManyGamesPlayed lichessTooManyGamesPlayed) {
        log.info("Scheduling Expired Timer Event for CHALLENGES");
        try {

            RetryIn retryIn = lichessTooManyGamesPlayed.getRetryIn();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(publishExpiredTimerEventGamesJob)
                    .withIdentity("PublishTooManyExpiredTimerEventTrigger_Game-" + UUID.randomUUID())
                    .startAt(DateBuilder.futureDate((int) retryIn.getSeconds(), DateBuilder.IntervalUnit.SECOND))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0) // Explicitly set repeat count to 0 (runs once)
                            .withIntervalInSeconds(0))
                    .build();

            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }

}
