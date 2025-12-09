package net.chesstango.sosa.init.configs;

import net.chesstango.sosa.init.steps.SendWorkerBusy;
import net.chesstango.sosa.init.steps.WaitGameStart;
import net.chesstango.sosa.init.steps.WriteGamePropertyFile;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {

    @Bean
    public Step waitGameStartStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  WaitGameStart waitGameStart) {
        return new StepBuilder("WaitGameStart", jobRepository)
                .tasklet(waitGameStart, transactionManager)
                .build();
    }

    @Bean
    public Step sendWorkerBusyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                   SendWorkerBusy sendWorkerBusy) {
        return new StepBuilder("SendWorkerBusy", jobRepository)
                .tasklet(sendWorkerBusy, transactionManager)
                .build();
    }

    @Bean
    public Step writeGamePropertyFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                          WriteGamePropertyFile writeGamePropertyFile) {
        return new StepBuilder("WriteGamePropertyFile", jobRepository)
                .tasklet(writeGamePropertyFile, transactionManager)
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

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobRepository jobRepository() {
        return new ResourcelessJobRepository();
    }
}
