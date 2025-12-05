package net.chesstango.sosa.worker;

import chariot.Client;
import chariot.ClientAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.lichess.LichessClient;
import net.chesstango.sosa.worker.lichess.LichessClientBean;
import net.chesstango.sosa.worker.lichess.LichessClientImp;
import net.chesstango.sosa.worker.lichess.LichessGameEventsReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class WorkerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

    private final String botToken;

    private final LichessClientBean lichessClient;
    private final LichessGameEventsReader lichessGameEventsReader;

    public WorkerApplication(@Value("${app.botToken}") String botToken,
                             LichessClientBean lichessClient,
                             LichessGameEventsReader lichessGameEventsReader) {
        this.botToken = botToken;
        this.lichessClient = lichessClient;
        this.lichessGameEventsReader = lichessGameEventsReader;
    }

    @Override
    public void run(String... args) {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(botToken);

        LichessClient lichessClientImp = new LichessClientImp(clientAuth);

        lichessClient.setImp(lichessClientImp);

        lichessGameEventsReader.run();

        log.info("LichessGameEventsReader triggered");
    }
}
