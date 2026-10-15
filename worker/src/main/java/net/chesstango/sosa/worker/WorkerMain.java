package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class WorkerMain implements CommandLineRunner, ExitCodeGenerator {

    private final JobOperator jobOperator;

    private final Job workerJob;

    private int exitCode = 0;

    public WorkerMain(JobOperator jobOperator, Job workerJob) {
        this.jobOperator = jobOperator;
        this.workerJob = workerJob;
    }

    @Override
    public void run(String @NonNull ... args) {
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();
        try {
            log.info("Starting job");
            JobExecution execution = jobOperator.start(workerJob, jobParameters);
            if (ExitStatus.COMPLETED.equals(execution.getExitStatus())) {
                log.info("Job completed successfully");
            } else {
                log.error("Job failed with exit status {}", execution.getExitStatus());
                exitCode = 1;
            }
            log.info("Job finished");
        } catch (Exception e) {
            log.error("Exception running job", e);
            exitCode = 2;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(WorkerMain.class, args)));
    }
}
