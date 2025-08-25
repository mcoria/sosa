package net.chesstango.sosa.master.configs;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
    @Scope("game")
    public String gameId() {
        return GameScope.getThreadConversationId();
    }

}
