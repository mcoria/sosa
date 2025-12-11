package net.chesstango.sosa.worker.configs;

import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class QuartzConfig {

    /*
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

     */
}
