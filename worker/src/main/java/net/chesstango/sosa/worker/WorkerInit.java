package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.GameEventsLoopException;
import net.chesstango.sosa.worker.events.GameEventsLoopFinished;
import net.chesstango.sosa.worker.lichess.GameEventsLoopReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class WorkerInit {
    private final ApplicationContext context;
    private final GameEventsLoopReader gameEventsLoopReader;
    private final ThreadPoolTaskExecutor taskExecutor;

    public WorkerInit(ApplicationContext context,
                      GameEventsLoopReader gameEventsLoopReader,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.context = context;
        this.gameEventsLoopReader = gameEventsLoopReader;
        this.taskExecutor = taskExecutor;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("ApplicationReadyEvent received");

        runBotEventsLoopReader();
    }

    /**
     * Es necesario volver a ejecutar el loop de lectura de eventos de lichess
     */
    @EventListener(GameEventsLoopException.class)
    public void onGameEventsLoopException() {
        log.info("GameEventsLoopException received");

        runBotEventsLoopReader();
    }

    /**
     * Es necesario volver a ejecutar el loop de lectura de eventos de lichess
     */
    @EventListener(GameEventsLoopFinished.class)
    public void onGameEventsLoopFinished() {
        // Specify exit code 0 for a clean exit, or another integer for an error
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

    void runBotEventsLoopReader() {
        taskExecutor.submit(gameEventsLoopReader);
    }
}
