package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.model.GoFast;
import net.chesstango.sosa.model.NewGame;
import net.chesstango.sosa.model.StartPosition;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mauricio Coria
 */
@Configuration
public class RabbitConfig {

    public static final String CHESS_TANGO_EXCHANGE = "chesstango.exchange";
    public static final String NEW_GAMES_QUEUE = "new_games.queue";
    public static final String NEW_GAMES_ROUTING_KEY = "new_games.key";

    @Bean
    public Queue demoQueue() {
        return new Queue(NEW_GAMES_QUEUE, false);
    }

    @Bean
    public DirectExchange demoExchange() {
        return new DirectExchange(CHESS_TANGO_EXCHANGE, false, false);
    }

    @Bean
    public Binding demoBinding(Queue demoQueue, DirectExchange demoExchange) {
        return BindingBuilder.bind(demoQueue).to(demoExchange).with(NEW_GAMES_ROUTING_KEY);
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
