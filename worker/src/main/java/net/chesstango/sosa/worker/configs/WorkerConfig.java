package net.chesstango.sosa.worker.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Mauricio Coria
 */

@Configuration
@PropertySource("file:C:\\java\\projects\\chess\\chess-utils\\engines\\game.properties")
public class WorkerConfig {
}
