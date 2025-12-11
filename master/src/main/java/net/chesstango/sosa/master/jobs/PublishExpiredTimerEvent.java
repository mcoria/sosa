package net.chesstango.sosa.master.jobs;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@PersistJobDataAfterExecution
@Component
@Slf4j
public class PublishExpiredTimerEvent extends QuartzJobBean {

    private final ApplicationEventPublisher applicationEventPublisher;

    public PublishExpiredTimerEvent(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            String expirationType = context.getJobDetail().getJobDataMap().getString("expirationType");

            if (expirationType.equals("GAMES")) {
                applicationEventPublisher.publishEvent(new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.GAMES));
            } else if (expirationType.equals("REQUESTS")) {
                applicationEventPublisher.publishEvent(new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.REQUESTS));
            } else {
                log.warn("ExpirationType not supported {}", expirationType);
            }

        } catch (Exception e) {
            log.error("Error executing PublishExpiredTimerEvent", e);
        }
    }
}
