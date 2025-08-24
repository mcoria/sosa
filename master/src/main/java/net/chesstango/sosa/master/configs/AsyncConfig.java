package net.chesstango.sosa.master.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author Mauricio Coria
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String GAME_TASK_EXECUTOR = "gameTaskExecutor";


    @Bean(name = GAME_TASK_EXECUTOR)
    public Executor gameTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("game-");
        exec.setCorePoolSize(32);
        exec.setMaxPoolSize(64);
        exec.setQueueCapacity(2000);
        exec.initialize();
        return exec;
    }

}
