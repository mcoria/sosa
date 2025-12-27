package net.chesstango.sosa.master.configs;

import chariot.Client;
import chariot.ClientAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LichessConfig {

    @Bean
    public ClientAuth lichessClientAuth(@Value("${app.botToken}") String botToken) {
        return Client.auth(botToken);
    }
}
