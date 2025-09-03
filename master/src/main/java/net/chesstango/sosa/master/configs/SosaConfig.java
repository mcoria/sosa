package net.chesstango.sosa.master.configs;

import net.chesstango.sosa.master.GameProducer;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessGame;
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

    public static final String WORKER_SCOPE = "worker_scope";

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope(WORKER_SCOPE, new GameScope());
        return configurer;
    }

    @Bean
    @Scope(value = WORKER_SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public LichessGame lichessGame(LichessClient client, GameProducer newGameProducer) {
        String workerId = GameScope.getThreadConversationId();
        if (workerId == null) {
            throw new IllegalStateException("No workerId found in ThreadConversation");
        }
        return new LichessGame(client, newGameProducer, workerId);
    }

    @Bean
    @Scope(value = WORKER_SCOPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public GameProducer newGameProducer(RabbitTemplate rabbitTemplate) {
        String workerId = GameScope.getThreadConversationId();
        if (workerId == null) {
            throw new IllegalStateException("No workerId found in ThreadConversation");
        }
        return new GameProducer(rabbitTemplate, workerId);
    }

}
