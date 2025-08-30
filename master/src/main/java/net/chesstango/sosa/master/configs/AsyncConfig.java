package net.chesstango.sosa.master.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mauricio Coria
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    public static final String GAME_LOOP_EXECUTOR = "gameLoopExecutor";

    @Bean(name = GAME_LOOP_EXECUTOR, destroyMethod = "shutdown")
    public ExecutorService gameLoopTaskExecutor(@Value("${app.maxSimultaneousGames}") int maxSimultaneousGames) {
        return Executors.newFixedThreadPool(maxSimultaneousGames);
    }

}
