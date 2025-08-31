package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.jobs.ChallengeJob;
import net.chesstango.sosa.master.jobs.StartupJob;
import org.quartz.*;
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
    public JobDetail startupJobDetails() {
        return JobBuilder.newJob(StartupJob.class)
                .withIdentity("startupJob")
                .withDescription("Runs once and completes")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger startupJobTrigger(JobDetail startupJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(startupJobDetails)
                .withIdentity("startupJobTrigger")
                .startNow()
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "app.challengeJob",
            havingValue = "true",
            matchIfMissing = false
    )
    public JobDetail challengeJobDetails() {
        return JobBuilder.newJob(ChallengeJob.class)
                .withIdentity("challengeJob")
                .withDescription("Runs once and completes")
                .storeDurably()
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "app.challengeJob",
            havingValue = "true",
            matchIfMissing = false
    )
    public Trigger challengeJobTrigger(JobDetail challengeJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(challengeJobDetails)
                .withIdentity("challengeJobTrigger")
                .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(2) // Repeat every second
                        .repeatForever()) // Repeat indefinitely
                .build();
    }
}
