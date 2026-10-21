package net.chesstango.sosa.master;

import chariot.model.UserAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessMainLoopFinished;
import net.chesstango.sosa.master.lichess.BotEventsLoopReader;
import net.chesstango.sosa.master.lichess.LichessClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class MasterInit implements Runnable {
    private final SosaState sosaState;
    private final LichessClient lichessClient;
    private final BotEventsLoopReader botEventsLoopReader;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MasterInit(SosaState sosaState,
                      LichessClient lichessClient,
                      BotEventsLoopReader botEventsLoopReader,
                      ApplicationEventPublisher applicationEventPublisher,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.sosaState = sosaState;
        this.lichessClient = lichessClient;
        this.botEventsLoopReader = botEventsLoopReader;
        this.applicationEventPublisher = applicationEventPublisher;
        this.taskExecutor = taskExecutor;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("ApplicationReadyEvent received");

        taskExecutor.submit(this);
    }

    /**
     * Es necesario volver a ejecutar el loop de lectura de eventos de lichess
     */
    @EventListener(LichessMainLoopFinished.class)
    public void onLichessMainLoopFinished() {
        log.info("LichessMainLoopFinished received");

        taskExecutor.submit(this);
    }

    @Override
    public void run() {
        try {
            log.info("Reading profile");

            UserAuth myProfile = lichessClient.getProfile();

            sosaState.setMyProfile(myProfile);

            botEventsLoopReader.run();
        } catch (Exception e) {
            log.error("Error detected", e);
        }

        try {
            Thread.sleep(1000);
            applicationEventPublisher.publishEvent(new LichessMainLoopFinished(this));
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        }
    }
}
