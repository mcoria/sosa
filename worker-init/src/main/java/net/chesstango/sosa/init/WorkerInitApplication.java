package net.chesstango.sosa.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * Este es un proceso en batch que:
 * <p>
 * - Notifica a master cuando un worker se ha iniciado
 * - Espera un mensaje de inicio de juego. Se asegura que solo se inicia un juego por vez.
 * - Escribe game.properties con los datos del juego
 * - Finaliza con exito para iniciar el worker
 *
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class WorkerInitApplication implements CommandLineRunner, ExitCodeGenerator {

    private final JobLauncher jobLauncher;

    private final Job myJob;

    private int exitCode = 0;

    public WorkerInitApplication(JobLauncher jobLauncher, Job myJob) {
        this.jobLauncher = jobLauncher;
        this.myJob = myJob;
    }

    @Override
    public void run(String... args) {
        JobParameters jobParameters = new JobParametersBuilder()
                .toJobParameters();
        try {
            jobLauncher.run(myJob, jobParameters);
        } catch (Exception e) {
            log.error("Error running job", e);
            exitCode = 1;
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
