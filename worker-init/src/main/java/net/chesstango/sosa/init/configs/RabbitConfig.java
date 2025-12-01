package net.chesstango.sosa.init.configs;

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

import static net.chesstango.sosa.messages.Constants.SOSA_EXCHANGE;

/**
 * @author Mauricio Coria
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public DirectExchange chessTangoExchange() {
        return new DirectExchange(SOSA_EXCHANGE, false, false);
    }

    @Bean
    public Queue workerQueue(@Value("${app.workerId}") String identity) {
        return new Queue(identity, false);
    }

    @Bean
    public Binding workerQueueBinding(Queue workerQueue, DirectExchange chessTangoExchange, @Value("${app.workerId}") String identity) {
        return BindingBuilder.bind(workerQueue).to(chessTangoExchange).with(identity);
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
