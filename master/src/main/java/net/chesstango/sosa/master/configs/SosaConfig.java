package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mauricio Coria
 */
@Configuration
public class SosaConfig {

    @Bean
    public Map<String, LichessGame> activeGames() {
        return Collections.synchronizedMap(new HashMap<>());
    }
}
