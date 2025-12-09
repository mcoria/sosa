package net.chesstango.sosa.init.configs;

import net.chesstango.sosa.messages.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static net.chesstango.sosa.messages.Constants.*;

/**
 * @author Mauricio Coria
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public DirectExchange chessTangoExchange() {
        return new DirectExchange(SOSA_EXCHANGE, false, true);
    }

    @Bean
    public Queue workerGamesQueue() {
        return new Queue(WORKER_GAMES_QUEUE, false, false, true);
    }

    @Bean
    public Binding workerQueueBinding(Queue workerGamesQueue, DirectExchange chessTangoExchange) {
        return BindingBuilder.bind(workerGamesQueue).to(chessTangoExchange).with(WORKER_GAMES_ROUTING_KEY);
    }

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
