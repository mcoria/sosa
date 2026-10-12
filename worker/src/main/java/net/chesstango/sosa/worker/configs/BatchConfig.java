package net.chesstango.sosa.worker.configs;

import net.chesstango.sosa.worker.steps.ConnectToLichess;
import net.chesstango.sosa.worker.steps.PlayGame;
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
    public Step playGameStep(JobRepository jobRepository,
                             PlayGame playGame) {
        return new StepBuilder("PlayGame", jobRepository)
                .tasklet(playGame)
                .build();
    }


    @Bean
    public Job workerJob(JobRepository jobRepository,
                         Step connectToLichessStep,
                         Step playGameStep) {
        return new JobBuilder("Worker", jobRepository)
                .start(connectToLichessStep)
                .next(playGameStep)
                .build();
    }


}
