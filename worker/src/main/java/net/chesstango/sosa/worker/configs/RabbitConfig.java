package net.chesstango.sosa.worker.configs;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String NEW_GAMES_QUEUE = "new_games.queue";

    @Bean
    public Queue demoQueue() {
        return new Queue(NEW_GAMES_QUEUE, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
