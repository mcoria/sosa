package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class MasterMain {
    public static void main(String[] args) {
        SpringApplication.run(MasterMain.class, args);
    }
}
