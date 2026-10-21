package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.jobs.ChallengeBotJob;
import net.chesstango.sosa.master.jobs.ExpireChallengesJob;
import net.chesstango.sosa.master.jobs.ExpireWorkersJob;
import net.chesstango.sosa.master.jobs.PublishExpiredTimerEventJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * @author Mauricio Coria
 */
@Configuration
public class QuartzConfig {

    @Bean
    @ConditionalOnProperty(name = "app.challengeJob", havingValue = "true")
    public JobDetail challengeBotJobDetails() {
        return JobBuilder.newJob(ChallengeBotJob.class)
                .withIdentity("ChallengeBotJob")
                .withDescription("Challenge Bot Job")
                .storeDurably()
                .build();
    }


    @Bean
    @ConditionalOnProperty(name = "app.challengeJob", havingValue = "true")
    public Trigger challengeBotJobTrigger(JobDetail challengeBotJobDetails, @Value("${app.challengeTime}") int challengeTime) {
        return TriggerBuilder.newTrigger()
                .forJob(challengeBotJobDetails)
                .withIdentity("ChallengeBotJobTrigger")
                .startAt(DateBuilder.futureDate(challengeTime, DateBuilder.IntervalUnit.MINUTE))
                .withSchedule(simpleSchedule()
                        .withIntervalInMinutes(challengeTime)   // Repeat every 3 minutes
                        .repeatForever())                       // Repeat indefinitely
                .build();
    }

    @Bean
    public JobDetail expireChallengesJobDetails(@Value("${app.challengeExpire}") int challengeExpire) {
        return JobBuilder.newJob(ExpireChallengesJob.class)
                .withIdentity("ExpireChallengesJob")
                .withDescription("Expire Challenges Job")
                .usingJobData("challengeExpire", Integer.toString(challengeExpire))
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger expireChallengesJobTrigger(JobDetail expireChallengesJobDetails, @Value("${app.challengeExpire}") int challengeExpire) {
        return TriggerBuilder.newTrigger()
                .forJob(expireChallengesJobDetails)
                .withIdentity("ExpireChallengesJobTrigger")
                .startAt(DateBuilder.futureDate(challengeExpire, DateBuilder.IntervalUnit.MINUTE))
                .withSchedule(
                        simpleSchedule()
                                .withIntervalInMinutes(challengeExpire) // Repeat every challengeExpire minutes
                                .repeatForever()                        // Repeat indefinitely
                )
                .build();
    }


    @Bean
    public JobDetail expireWorkersJobDetails(@Value("${app.workerExpire}") int workerExpire) {
        return JobBuilder.newJob(ExpireWorkersJob.class)
                .withIdentity("ExpireWorkersJob")
                .withDescription("Expire Workers Job")
                .usingJobData("workerExpire", Integer.toString(workerExpire))
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger expireWorkersJobTrigger(JobDetail expireWorkersJobDetails, @Value("${app.workerExpire}") int workerExpire) {
        return TriggerBuilder.newTrigger()
                .forJob(expireWorkersJobDetails)
                .withIdentity("ExpireWorkersJobTrigger")
                .startAt(DateBuilder.futureDate(workerExpire, DateBuilder.IntervalUnit.MINUTE))
                .withSchedule(simpleSchedule()
                        .withIntervalInMinutes(workerExpire) // Repeat every workerExpire minutes
                        .repeatForever())                    // Repeat indefinitely
                .build();
    }

    @Bean
    public JobDetail publishExpiredTimerEventGamesJob() {
        return JobBuilder.newJob(PublishExpiredTimerEventJob.class)
                .withIdentity("PublishTooManyExpiredTimerEvent_Game")
                .usingJobData("expirationType", "GAMES")
                .storeDurably()
                .build();

    }
}
