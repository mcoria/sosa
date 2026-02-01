package net.chesstango.sosa.init.configs;

import net.chesstango.sosa.init.steps.SendWorkerBusy;
import net.chesstango.sosa.init.steps.WaitGameStart;
import net.chesstango.sosa.init.steps.WriteGamePropertyFile;
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
    public Step waitGameStartStep(JobRepository jobRepository,
                                  WaitGameStart waitGameStart) {
        return new StepBuilder("WaitGameStart", jobRepository)
                .tasklet(waitGameStart)
                .build();
    }

    @Bean
    public Step sendWorkerBusyStep(JobRepository jobRepository,
                                   SendWorkerBusy sendWorkerBusy) {
        return new StepBuilder("SendWorkerBusy", jobRepository)
                .tasklet(sendWorkerBusy)
                .build();
    }

    @Bean
    public Step writeGamePropertyFileStep(JobRepository jobRepository,
                                          WriteGamePropertyFile writeGamePropertyFile) {
        return new StepBuilder("WriteGamePropertyFile", jobRepository)
                .tasklet(writeGamePropertyFile)
                .build();
    }

    @Bean
    public Job workerInitJob(JobRepository jobRepository,
                             Step waitGameStartStep,
                             Step sendWorkerBusyStep,
                             Step writeGamePropertyFileStep) {
        return new JobBuilder("WorkerInit", jobRepository)
                .start(waitGameStartStep)
                .next(sendWorkerBusyStep)
                .next(writeGamePropertyFileStep)
                .build();
    }


}
