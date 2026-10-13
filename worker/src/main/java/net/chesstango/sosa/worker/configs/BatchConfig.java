package net.chesstango.sosa.worker.configs;

import net.chesstango.sosa.worker.steps.ConnectToLichess;
import net.chesstango.sosa.worker.steps.OpenTango;
import net.chesstango.sosa.worker.steps.ReadGameEvents;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing // This annotation sets up the default beans, but we override the repository
public class BatchConfig {

    @Bean
    public Step connectToLichessStep(JobRepository jobRepository,
                                     ConnectToLichess connectToLichess) {
        return new StepBuilder("ConnectToLichess", jobRepository)
                .tasklet(connectToLichess)
                .build();
    }

    @Bean
    public Step openTangoStep(JobRepository jobRepository,
                              OpenTango openTango) {
        return new StepBuilder("OpenTango", jobRepository)
                .tasklet(openTango)
                .build();
    }


    @Bean
    public Step readGameEventsStep(JobRepository jobRepository,
                             ReadGameEvents readGameEvents) {
        return new StepBuilder("ReadGameEvents", jobRepository)
                .tasklet(readGameEvents)
                .build();
    }

    @Bean
    public Job workerJob(JobRepository jobRepository,
                         Step connectToLichessStep,
                         Step openTangoStep,
                         Step readGameEventsStep) {
        return new JobBuilder("Worker", jobRepository)
                .start(connectToLichessStep)
                .next(openTangoStep)
                .next(readGameEventsStep)
                .build();
    }


}
