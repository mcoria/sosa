package net.chesstango.sosa.master.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class QueryScheduler {
    private final Scheduler scheduler;
    private final JobDetail challengeBotJobDetails;

    public QueryScheduler(Scheduler scheduler,
                          @Autowired(required = false) JobDetail challengeBotJobDetails) {
        this.scheduler = scheduler;
        this.challengeBotJobDetails = challengeBotJobDetails;
    }


    public ScheduleStatus getStatus() {
        return new ScheduleStatus(true);
    }
}
