package net.chesstango.sosa.master.lichess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.UserAuth;
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

        UserAuth profile = lichessClient.getProfile();

        System.out.println(profile);

    }
}
