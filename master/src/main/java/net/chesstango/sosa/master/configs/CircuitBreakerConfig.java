package net.chesstango.sosa.master.configs;

import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;


import java.time.Duration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> specificCustomizer() {
        return factory -> {
            factory.configure(builder -> builder
                    .retryPolicy(RetryPolicy.withMaxRetries(1))
                    .openTimeout(Duration.ofSeconds(61 * 3))
                    .resetTimeout(Duration.ofHours(1))
                    .build(), "lichess");
        };
    }
}
