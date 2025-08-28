package net.chesstango.sosa.worker.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
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
    public DirectExchange chessTangoExchange() {
        return new DirectExchange(CHESS_TANGO_EXCHANGE, false, false);
    }

    @Bean
    public Queue gameQueue(@Value("${gameId}") String gameId) {
        return new Queue(gameId, false);
    }

    @Bean
    public Binding gameQueueBinding(Queue gameQueue, DirectExchange chessTangoExchange, @Value("${gameId}") String gameId) {
        return BindingBuilder.bind(gameQueue).to(chessTangoExchange).with(gameId);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true); // for returns when routing fails
        return template;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
