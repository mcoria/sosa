package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.OnceJob;
import net.chesstango.sosa.master.PeriodicJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail sampleJobDetail() {
        return JobBuilder.newJob(PeriodicJob.class)
                .withIdentity("sampleJob")
                .withDescription("Runs a sample task")
                .storeDurably()              // keep job even without trigger
                .requestRecovery(true)       // re-run if the scheduler crashed mid-run
                .usingJobData("key", "value")
                .build();
    }

    @Bean
    public Trigger sampleJobTrigger(JobDetail sampleJobDetail) {
        // Simple schedule (every 10 seconds)
        SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(10)
                .repeatForever()
                .withMisfireHandlingInstructionNextWithRemainingCount(); // choose misfire policy

        //   CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")
        //       .withMisfireHandlingInstructionDoNothing();

        return TriggerBuilder.newTrigger()
                .forJob(sampleJobDetail)
                .withIdentity("sampleTrigger")
                .withSchedule(schedule)
                .build();
    }

    @Bean
    public JobDetail oneTimeJobDetail() {
        return JobBuilder.newJob(OnceJob.class)
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
