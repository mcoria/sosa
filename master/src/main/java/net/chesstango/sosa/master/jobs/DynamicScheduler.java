package net.chesstango.sosa.master.jobs;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.ChallengeEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class DynamicScheduler implements ApplicationListener<SosaEvent> {
    private final Scheduler scheduler;

    @Value("${app.gameWatchDog}")
    @Setter
    private Boolean gameWatchDogEnabled;

    @Value("${app.gameExpire}")
    @Setter
    public int GAME_EXPIRE = 30;

    @Value("${app.challengeExpire}")
    @Setter
    public int CHALLENGE_EXPIRE = 15;

    public DynamicScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void onApplicationEvent(SosaEvent event) {
        /*
        if (event instanceof ChallengeEvent challengeEvent) {
            if (Objects.requireNonNull(challengeEvent.getType()) == ChallengeEvent.Type.CHALLENGE_ACCEPTED) {
                scheduleChallengeWatchDog(challengeEvent.getChallengeId());
            }
        } else if (event instanceof GameStartEvent gameStartEvent) {
            if (gameWatchDogEnabled) {
                scheduleGameWatchDog(gameStartEvent.getGameId());
            }
        }
         */
    }

    private void scheduleChallengeWatchDog(String challengeId) {
        try {
            JobDetail job = JobBuilder.newJob(ChallengeWatchDogJob.class)
                    .withIdentity(String.format("challengeWatchDogJob-%s", challengeId))
                    .usingJobData("challengeId", challengeId)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(String.format("challengeWatchDogTrigger-%s", challengeId))
                    .startAt(DateBuilder.futureDate(CHALLENGE_EXPIRE, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("SchedulerException:", e);
            throw new RuntimeException(e);
        }
    }

    private void scheduleGameWatchDog(String gameId) {
        try {
            JobDetail job = JobBuilder.newJob(GameWatchDogJob.class)
                    .withIdentity(String.format("gameWatchDogJob-%s", gameId))
                    .usingJobData("gameId", gameId)
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(String.format("gameWatchDogTrigger-%s", gameId))
                    .startAt(DateBuilder.futureDate(GAME_EXPIRE, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("SchedulerException:", e);
            throw new RuntimeException(e);
        }
    }

}
