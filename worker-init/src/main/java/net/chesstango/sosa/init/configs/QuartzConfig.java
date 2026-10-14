package net.chesstango.sosa.init.configs;


import net.chesstango.sosa.init.jobs.SendWorkerReady;
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
    public JobDetail sendWorkerReadyJobDetails() {
        return JobBuilder.newJob(SendWorkerReady.class)
                .withIdentity("SendWorkerReadyJob")
                .withDescription("Send Worker Ready Job")
                .storeDurably()
                .build();
    }


    @Bean
    public Trigger sendWorkerReadyJobTrigger(JobDetail sendWorkerReadyJobDetails) {
        return TriggerBuilder.newTrigger()
                .forJob(sendWorkerReadyJobDetails)
                .withIdentity("SendWorkerReadyJobTrigger")
                .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(60) // Repeat every second
                        .repeatForever())          // Repeat indefinitely
                .build();
    }
}
