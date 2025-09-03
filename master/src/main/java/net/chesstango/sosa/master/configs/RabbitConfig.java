package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.messages.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class RabbitConfig {

    public static final String BOTS_QUEUE = "bots";
    public static final String BOTS_ROUTING_KEY = "bots_rk";

    @Bean
    public DirectExchange chessTangoExchange() {
        return new DirectExchange(Constants.CHESS_TANGO_EXCHANGE, false, false);
    }

    @Bean
    public Queue workerRespondsQueue() {
        return new Queue(Constants.MASTER_QUEUE, false);
    }

    @Bean
    public Binding workerRespondsBinding(Queue workerRespondsQueue, DirectExchange chessTangoExchange) {
        return BindingBuilder.bind(workerRespondsQueue).to(chessTangoExchange).with(Constants.MASTER_ROUTING_KEY);
    }

    @Bean
    public Queue botsQueue() {
        return new Queue(BOTS_QUEUE, false);
    }

    @Bean
    public Binding botsBinding(Queue botsQueue, DirectExchange chessTangoExchange) {
        return BindingBuilder.bind(botsQueue).to(chessTangoExchange).with(BOTS_ROUTING_KEY);
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
