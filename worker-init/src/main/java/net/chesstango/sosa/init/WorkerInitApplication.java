package net.chesstango.sosa.init;

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
 *
 * Este es un proceso en batch que:
 * <p>
 * - Notifica a master cuando un worker se ha iniciado
 * - Espera un mensaje de inicio de juego.
 * -- Verificar que el mensaje de inicio no se procesó con anterioridad.
 * - Escribe game.properties con los datos del juego
 * - Finaliza con exito para iniciar el worker
 *
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class WorkerInitApplication implements CommandLineRunner, ExitCodeGenerator {

    private final JobOperator jobOperator;

    private final Job workerInitJob;

    private int exitCode = 0;

    public WorkerInitApplication(JobOperator jobOperator, Job workerInitJob) {
        this.jobOperator = jobOperator;
        this.workerInitJob = workerInitJob;
    }

    @Override
    public void run(String @NonNull ... args) {
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();
        try {
            log.info("Starting job");
            JobExecution execution = jobOperator.start(workerInitJob, jobParameters);
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
        System.exit(SpringApplication.exit(SpringApplication.run(WorkerInitApplication.class, args)));
    }
}
