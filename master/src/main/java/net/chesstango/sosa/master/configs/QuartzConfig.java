package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.jobs.PublishExpiredTimerEvent;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class QuartzConfig {

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
