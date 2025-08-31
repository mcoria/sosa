package net.chesstango.sosa.init.configs;

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
    public static final String CHESS_TANGO_EXCHANGE = "chesstango.exchange";

    public static final String MASTER_REQUESTS_QUEUE = "master_requests";
    public static final String MASTER_REQUESTS_ROUTING_KEY = "master_requests_rk";

    public static final String WORKER_RESPONDS_QUEUE = "worker_responds";
    public static final String WORKER_RESPONDS_ROUTING_KEY = "worker_responds_rk";

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        rabbitTemplate.setMandatory(true); // for returns when routing fails
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
