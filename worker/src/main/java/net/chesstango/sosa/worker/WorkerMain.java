package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class WorkerMain {

    public static void main(String[] args) {
        SpringApplication.run(WorkerMain.class, args);
    }
}
