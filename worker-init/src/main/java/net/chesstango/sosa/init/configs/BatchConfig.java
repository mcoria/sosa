package net.chesstango.sosa.init.configs;

import net.chesstango.sosa.init.steps.ReadGame;
import net.chesstango.sosa.init.steps.RegisterWorker;
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
    public Step registerWorkerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, RegisterWorker registerWorker) {
        return new StepBuilder("registerWorkerStep", jobRepository)
                .tasklet(registerWorker, transactionManager)
                .build();
    }

    @Bean
    public Step readGameStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, ReadGame readGame) {
        return new StepBuilder("readGameStep", jobRepository)
                .tasklet(readGame, transactionManager)
                .build();
    }

    @Bean
    public Step writeGamePropertyFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, WriteGamePropertyFile writeGamePropertyFile) {
        return new StepBuilder("writeGamePropertyFile", jobRepository)
                .tasklet(writeGamePropertyFile, transactionManager)
                .build();
    }

    @Bean
    public Job myJob(JobRepository jobRepository, Step registerWorkerStep, Step readGameStep, Step writeGamePropertyFileStep) {
        return new JobBuilder("myJob", jobRepository)
                .start(registerWorkerStep)
                .next(readGameStep)
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
