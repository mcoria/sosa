package net.chesstango.sosa.worker.configs;

import net.chesstango.sosa.worker.jobs.GameAbortJob;
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
    public JobDetail gameAbortJobDetails() {
        return JobBuilder.newJob(GameAbortJob.class)
                .withIdentity("challengeJob")
                .withDescription("Challenge Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger gameAbortJobTrigger(JobDetail gameAbortJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(gameAbortJobDetails)
                .withIdentity("gameAbortJobTrigger")
                .startAt(DateBuilder.futureDate(2, DateBuilder.IntervalUnit.MINUTE))
                .withSchedule(simpleSchedule().withRepeatCount(0))  // No repeat
                .build();
    }
}
