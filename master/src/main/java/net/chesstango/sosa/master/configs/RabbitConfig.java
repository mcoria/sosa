package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.messages.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mauricio Coria
 */
@Configuration
public class RabbitConfig {

    public static final String MASTER_BOTS_QUEUE = "sosa-master-bots";
    public static final String MASTER_BOTS_ROUTING_KEY = "sosa_master_bots_rk";

    @Bean
    public DirectExchange chessTangoExchange() {
        return new DirectExchange(Constants.SOSA_EXCHANGE, false, true);
    }

    @Bean
    public Queue masterQueue() {
        return new Queue(Constants.MASTER_QUEUE, false, true, true);
    }

    @Bean
    public Binding workerRespondsBinding(Queue masterQueue, DirectExchange chessTangoExchange) {
        return BindingBuilder.bind(masterQueue).to(chessTangoExchange).with(Constants.MASTER_ROUTING_KEY);
    }

    @Bean
    public Queue masterBotsQueue() {
        return new Queue(MASTER_BOTS_QUEUE, false);
    }

    @Bean
    public Binding botsBinding(Queue masterBotsQueue, DirectExchange chessTangoExchange) {
        return BindingBuilder.bind(masterBotsQueue).to(chessTangoExchange).with(MASTER_BOTS_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true); // for returns when routing fails
        return template;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        // Can be constructed with an internal ObjectMapper instance or a custom one
        return new JacksonJsonMessageConverter();
    }
}
