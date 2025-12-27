package net.chesstango.sosa.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.worker.events.LichessConnected;
import net.chesstango.sosa.worker.lichess.LichessGameEventsReader;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class WorkerInit {
    private final LichessGameEventsReader lichessGameEventsReader;
    private final ThreadPoolTaskExecutor taskExecutor;

    public WorkerInit(LichessGameEventsReader lichessGameEventsReader, ThreadPoolTaskExecutor taskExecutor) {
        this.lichessGameEventsReader = lichessGameEventsReader;
        this.taskExecutor = taskExecutor;
    }

    @EventListener(LichessConnected.class)
    public void onLichessConnected() {
        log.info("LichessConnected event received");
        taskExecutor.submit(lichessGameEventsReader);
    }
}
