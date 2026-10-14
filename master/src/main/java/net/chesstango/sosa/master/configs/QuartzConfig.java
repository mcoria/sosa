package net.chesstango.sosa.master.configs;

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
