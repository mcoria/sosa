package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.NewGameProducer;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * @author Mauricio Coria
 */
@Configuration
public class SosaConfig {

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("game", new GameScope());
        return configurer;
    }

    @Bean
    @Scope(value = "game", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public LichessGame lichessGame(LichessClient client) {
        String gameId = GameScope.getThreadConversationId();
        if (gameId == null) {
            throw new IllegalStateException("No gameId found in ThreadConversation");
        }
        return new LichessGame(client, gameId);
    }

    @Bean
    @Scope(value = "game", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public NewGameProducer newGameProducer(AmqpAdmin amqpAdmin, DirectExchange demoExchange, RabbitTemplate rabbitTemplate) {
        String gameId = GameScope.getThreadConversationId();
        if (gameId == null) {
            throw new IllegalStateException("No gameId found in ThreadConversation");
        }
        return new NewGameProducer(amqpAdmin, demoExchange, rabbitTemplate, gameId);
    }

}
