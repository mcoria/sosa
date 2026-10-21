package net.chesstango.sosa.master.lichess;

import chariot.Client;
import chariot.ClientAuth;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.mock;

/**
 * @author Mauricio Coria
 */
public class LichessClientMain {

    public static void main(String[] args) {

        ClientAuth clientAuth = Client.auth("");

        LichessErrorParser errorParser = new LichessErrorParser();

        LichessClientImp lichessClient = new LichessClientImp(clientAuth, errorParser);

        lichessClient.gameMove("vJV8bIlk", "c3b1");

    }
}
