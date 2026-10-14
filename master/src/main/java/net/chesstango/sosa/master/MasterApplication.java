package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessMainEventsReader;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class MasterApplication implements CommandLineRunner {

    private final LichessMainEventsReader lichessMainEventsReader;

    public MasterApplication(LichessMainEventsReader lichessMainEventsReader) {
        this.lichessMainEventsReader = lichessMainEventsReader;
    }

    @Override
    public void run(String @NonNull ... args) {

        lichessMainEventsReader.run();
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(MasterApplication.class, args);
    }

}
