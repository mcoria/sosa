package net.chesstango.sosa.init.configs;

import net.chesstango.sosa.init.steps.ReadGame;
import net.chesstango.sosa.init.steps.RegisterWorker;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class BatchConfig {

    // Define your ItemReader, ItemProcessor, and ItemWriter beans here
    @Bean
    public ItemReader<String> itemReader() {
        // Example: a simple reader that returns a few strings
        return new ItemReader<String>() {
            private int count = 0;
            private final String[] data = {"item1", "item2", "item3"};

            @Override
            public String read() {
                if (count < data.length) {
                    return data[count++];
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<String, String> itemProcessor() {
        return String::toUpperCase; // Example: converts to uppercase
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println("Writing: " + item);
            }
        };
    }

    @Bean
    public Step myStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("myStep", jobRepository)
                .<String, String>chunk(1, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

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
    public Job myJob(JobRepository jobRepository, Step registerWorkerStep, Step readGameStep) {
        return new JobBuilder("myJob", jobRepository)
                .start(registerWorkerStep)
                .next(readGameStep)
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
