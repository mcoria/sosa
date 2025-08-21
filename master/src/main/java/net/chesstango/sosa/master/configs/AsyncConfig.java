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


    @Bean(name = "ioBoundExecutor")
    public Executor ioBoundExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setThreadNamePrefix("io-");
        exec.setCorePoolSize(32);
        exec.setMaxPoolSize(64);
        exec.setQueueCapacity(2000);
        exec.initialize();
        return exec;
    }

}
