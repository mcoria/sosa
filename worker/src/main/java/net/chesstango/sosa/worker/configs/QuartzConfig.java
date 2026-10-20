package net.chesstango.sosa.worker.configs;

import net.chesstango.sosa.worker.jobs.GameAbortJob;
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
    @ConditionalOnProperty(name = "app.gameAbort", havingValue = "true")
    public JobDetail gameAbortJobDetails() {
        return JobBuilder.newJob(GameAbortJob.class)
                .withIdentity("GameAbortJob")
                .withDescription("GameAbort Job")
                .storeDurably()
                .build();
    }


    @Bean
    @ConditionalOnProperty(name = "app.gameAbort", havingValue = "true")
    public Trigger gameAbortJobTrigger(JobDetail gameAbortJobDetails, @Value("${app.gameAbortTimer}") int gameAbortTimer) {
        return TriggerBuilder.newTrigger()
                .forJob(gameAbortJobDetails)
                .withIdentity("gameAbortJobTrigger")
                .startAt(DateBuilder.futureDate(gameAbortTimer, DateBuilder.IntervalUnit.MINUTE))
                .withSchedule(simpleSchedule().withRepeatCount(0))  // No repeat
                .build();
    }
}
