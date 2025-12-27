package net.chesstango.sosa.init.configs;


import net.chesstango.sosa.init.jobs.ChallengeJob;
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
    public JobDetail challengeJobDetails() {
        return JobBuilder.newJob(ChallengeJob.class)
                .withIdentity("challengeJob")
                .withDescription("Challenge Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger challengeJobTrigger(JobDetail challengeJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(challengeJobDetails)
                .withIdentity("challengeJobTrigger")
                .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(45) // Repeat every second
                        .repeatForever()) // Repeat indefinitely
                .build();
    }
}
