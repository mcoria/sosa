package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.jobs.StartupJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class QuartzConfig {


    @Bean
    public JobDetail oneTimeJobDetail() {
        return JobBuilder.newJob(StartupJob.class)
                .withIdentity("oneTimeJob")
                .withDescription("Runs once and completes")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger oneTimeJobTrigger(JobDetail oneTimeJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(oneTimeJobDetail)
                .withIdentity("oneTimeTrigger")
                .startAt(DateBuilder.futureDate(15, DateBuilder.IntervalUnit.SECOND))
                .build();
    }
}
