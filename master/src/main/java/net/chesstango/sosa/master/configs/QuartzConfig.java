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
    public JobDetail startupJobDetail() {
        return JobBuilder.newJob(StartupJob.class)
                .withIdentity("startupJob")
                .withDescription("Runs once and completes")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger oneTimeJobTrigger(JobDetail startupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(startupJobDetail)
                .withIdentity("startupJobTrigger")
                .startNow()
                .build();
    }
}
