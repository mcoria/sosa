package net.chesstango.sosa.master.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.RetryIn;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final JobDetail challengeBotJobDetails;

    private final JobDetail publishExpiredTimerEventRequestsJob;

    private final JobDetail publishExpiredTimerEventGamesJob;

    private final int tooManyRequestExpire;

    private final boolean challengeJob;

    public DynamicScheduler(Scheduler scheduler,
                            @Autowired(required = false) JobDetail challengeBotJobDetails,
                            JobDetail publishExpiredTimerEventRequestsJob,
                            JobDetail publishExpiredTimerEventGamesJob,
                            @Value("${app.tooManyRequestExpire}") int tooManyRequestExpire,
                            @Value("${app.challengeJob}") boolean challengeJob) {
        this.scheduler = scheduler;
        this.challengeBotJobDetails = challengeBotJobDetails;
        this.publishExpiredTimerEventRequestsJob = publishExpiredTimerEventRequestsJob;
        this.publishExpiredTimerEventGamesJob = publishExpiredTimerEventGamesJob;
        this.tooManyRequestExpire = tooManyRequestExpire;
        this.challengeJob = challengeJob;
    }

    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequestsSent() {
        try {
            log.info("Scheduling Expired Timer Event for lichess REQUESTS");

            Trigger trigger = TriggerBuilder.newTrigger()
                    .forJob(publishExpiredTimerEventRequestsJob)
                    .withIdentity("PublishTooManyExpiredTimerEventTrigger_Requests-" + UUID.randomUUID())
                    .startAt(DateBuilder.futureDate(tooManyRequestExpire, DateBuilder.IntervalUnit.HOUR))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withRepeatCount(0) // Explicitly set repeat count to 0 (runs once)
                            .withIntervalInSeconds(0))
                    .build();
            scheduler.scheduleJob(trigger);

            if (challengeJob) {
                log.info("Pausing Challenge Bot Job");
                scheduler.pauseJob(challengeBotJobDetails.getKey());
            }

        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }


    @EventListener
    public void onLichessTooManyGamesPlayed(LichessTooManyGamesPlayed lichessTooManyGamesPlayed) {
        try {
            log.info("Scheduling Expired Timer Event for accepting CHALLENGES again");

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


    @EventListener
    public void onLichessTooManyExpired(LichessTooManyExpired lichessTooManyExpired) {
        try {
            switch (lichessTooManyExpired.getExpirationType()) {
                case GAMES:
                    break;
                case REQUESTS:
                    resumeChallengeBotJob();
                    break;
                default:
                    break;
            }
        } catch (SchedulerException e) {
            log.error("Error scheduling:", e);
            throw new RuntimeException(e);
        }
    }

    void resumeChallengeBotJob() throws SchedulerException {
        if (challengeJob) {
            log.info("Resuming Challenge Bot Job");
            scheduler.resumeJob(challengeBotJobDetails.getKey());
        }
    }

}
