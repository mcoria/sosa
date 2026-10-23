package net.chesstango.sosa.master.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class QueryScheduler {
    private final Scheduler scheduler;
    private final Trigger challengeBotJobTrigger;

    public QueryScheduler(Scheduler scheduler,
                          @Autowired(required = false) Trigger challengeBotJobTrigger) {
        this.scheduler = scheduler;
        this.challengeBotJobTrigger = challengeBotJobTrigger;
    }


    public ScheduleStatus getStatus() {
        boolean challengeJobEnabled = false;
        Date challengeJobLastExecution = null;

        if (challengeBotJobTrigger != null) {
            try {

                // 1 - Get the state of the challenge bot job trigger
                Trigger.TriggerState state = scheduler.getTriggerState(challengeBotJobTrigger.getKey());
                // NORMAL = waiting to fire; BLOCKED = active but blocked by a concurrent execution
                if (Trigger.TriggerState.NORMAL.equals(state) || Trigger.TriggerState.BLOCKED.equals(state)) {
                    challengeJobEnabled = true; // Found an active/enabled trigger
                }

                // 2 - Get the last execution time of the challenge bot job trigger
                challengeJobLastExecution = challengeBotJobTrigger.getPreviousFireTime();

            } catch (SchedulerException e) {
                log.error("Error getting TriggerState", e);
            }
        }

        return new ScheduleStatus(challengeJobEnabled, challengeJobLastExecution);
    }
}
