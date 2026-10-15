package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.jobs.ChallengeBotJob;
import net.chesstango.sosa.master.jobs.ExpireChallengesJob;
import net.chesstango.sosa.master.jobs.ExpireWorkersJob;
import net.chesstango.sosa.master.jobs.PublishExpiredTimerEvent;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * @author Mauricio Coria
 */
@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail challengeBotJobDetails() {
        return JobBuilder.newJob(ChallengeBotJob.class)
                .withIdentity("ChallengeBotJob")
                .withDescription("Challenge Bot Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger challengeBotJobTrigger(JobDetail challengeBotJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(challengeBotJobDetails)
                .withIdentity("ChallengeBotJobTrigger")
                .startAt(DateBuilder.futureDate(60, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(180) // Repeat every 3 minutes
                        .repeatForever())           // Repeat indefinitely
                .build();
    }

    @Bean
    public JobDetail expireChallengesJobDetails() {
        return JobBuilder.newJob(ExpireChallengesJob.class)
                .withIdentity("ExpireChallengesJob")
                .withDescription("Send Worker Ready Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger expireChallengesJobTrigger(JobDetail expireChallengesJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(expireChallengesJobDetails)
                .withIdentity("ExpireChallengesJobTrigger")
                .startAt(DateBuilder.futureDate(300, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(300) // Repeat every 5 minutes
                        .repeatForever())           // Repeat indefinitely
                .build();
    }


    @Bean
    public JobDetail expireWorkersJobDetails() {
        return JobBuilder.newJob(ExpireWorkersJob.class)
                .withIdentity("ExpireWorkersJob")
                .withDescription("Send Worker Ready Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger expireWorkersJobTrigger(JobDetail expireWorkersJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(expireWorkersJobDetails)
                .withIdentity("ExpireWorkersJobTrigger")
                .startAt(DateBuilder.futureDate(120, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(300) // Repeat every 5 minutes
                        .repeatForever())           // Repeat indefinitely
                .build();
    }


    @Bean
    public JobDetail publishExpiredTimerEventRequestsJob(){
        return JobBuilder.newJob(PublishExpiredTimerEvent.class)
                .withIdentity("PublishTooManyExpiredTimerEvent_Requests")
                .usingJobData("expirationType", "REQUESTS")
                .storeDurably()
                .build();

    }

    @Bean
    public JobDetail publishExpiredTimerEventGamesJob(){
        return JobBuilder.newJob(PublishExpiredTimerEvent.class)
                .withIdentity("PublishTooManyExpiredTimerEvent_Game")
                .usingJobData("expirationType", "GAMES")
                .storeDurably()
                .build();

    }
}
