package net.chesstango.sosa.master.scheduler;

import java.util.Date;

/**
 * @author Mauricio Coria
 */
public record ScheduleStatus(boolean challengeJobEnabled,
                             Date challengeJobLastExecution,
                             Date challengeJobNextExecution) {
}
