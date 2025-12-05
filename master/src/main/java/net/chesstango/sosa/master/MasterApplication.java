package net.chesstango.sosa.master;

import chariot.Client;
import chariot.ClientAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Mauricio Coria
 */
@SpringBootApplication
@Slf4j
public class MasterApplication implements CommandLineRunner  {

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    private final String botToken;

    private final LichessClientBean lichessClient;

    private final LichessChallengerBot lichessChallengerBot;

    private final LichessMainEventsReader lichessMainEventsReader;

    public MasterApplication(@Value("${app.botToken}") String botToken,
                             LichessClientBean lichessClient,
                             LichessChallengerBot lichessChallengerBot,
                             LichessMainEventsReader lichessMainEventsReader) {
        this.botToken = botToken;
        this.lichessClient = lichessClient;
        this.lichessChallengerBot = lichessChallengerBot;
        this.lichessMainEventsReader = lichessMainEventsReader;
    }

    @Override
    public void run(String... args) {
        log.info("Connecting to Lichess");

        ClientAuth clientAuth = Client.auth(botToken);

        LichessClient lichessClientImp = new LichessClientImp(clientAuth);

        lichessClient.setImp(lichessClientImp);

        lichessChallengerBot.updateRating();

        lichessMainEventsReader.run();

        log.info("MasterApplication initialized");
    }
}
