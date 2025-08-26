package net.chesstango.sosa.worker.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Mauricio Coria
 */

@Configuration
@PropertySource("file:${WORKER_INIT_DIRECTORY}\\game.properties")
public class WorkerConfig {
}
