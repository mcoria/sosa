package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import org.jspecify.annotations.NonNull;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import static net.chesstango.sosa.master.events.LichessTooManyExpired.ExpirationType.GAMES;
import static net.chesstango.sosa.master.events.LichessTooManyExpired.ExpirationType.REQUESTS;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Component
@Slf4j
public class PublishExpiredTimerEvent extends QuartzJobBean {

    private final ApplicationEventPublisher applicationEventPublisher;

    public PublishExpiredTimerEvent(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {
        try {
            String expirationType = context.getJobDetail().getJobDataMap().getString("expirationType");

            if (expirationType.equals("GAMES")) {
                applicationEventPublisher.publishEvent(new LichessTooManyExpired(this, GAMES));
            } else if (expirationType.equals("REQUESTS")) {
                applicationEventPublisher.publishEvent(new LichessTooManyExpired(this, REQUESTS));
            } else {
                log.warn("ExpirationType not supported {}", expirationType);
            }

        } catch (Exception e) {
            log.error("Error executing PublishExpiredTimerEvent", e);
        }
    }
}
