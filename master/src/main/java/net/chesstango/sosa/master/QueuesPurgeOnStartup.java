package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.master.configs.RabbitConfig.MASTER_REQUESTS_QUEUE;

/**
 * @author Mauricio Coria
 */
@Component
@Slf4j
public class QueuesPurgeOnStartup implements SmartLifecycle {

    private final AmqpAdmin amqpAdmin;

    private volatile boolean running = false;

    public QueuesPurgeOnStartup(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void start() {
        log.info("Purging queues on startup");
        amqpAdmin.purgeQueue(MASTER_REQUESTS_QUEUE, false);
        amqpAdmin.purgeQueue(MASTER_REQUESTS_QUEUE, false);
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Execute very late in the startup process
    }
}
