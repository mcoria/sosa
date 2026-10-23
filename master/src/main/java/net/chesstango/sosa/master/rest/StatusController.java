package net.chesstango.sosa.master.rest;

import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.SosaStatus;
import net.chesstango.sosa.master.scheduler.QueryScheduler;
import net.chesstango.sosa.master.scheduler.ScheduleStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mauricio Coria
 */
@RestController
@RequestMapping("/status") //
public class StatusController {

    private final SosaState sosaState;
    private final QueryScheduler queryScheduler;

    public StatusController(SosaState sosaState,
                            QueryScheduler queryScheduler) {
        this.sosaState = sosaState;
        this.queryScheduler = queryScheduler;
    }


    @GetMapping
    public Map<String, Object> get() {
        Map<String, Object> result = new HashMap<>();
        SosaStatus sosaStatus = sosaState.getStatus();
        ScheduleStatus schedulerStatus = queryScheduler.getStatus();

        result.put("availableWorkers", sosaStatus.availableWorkers());
        result.put("inProgressChallenges", sosaStatus.inProgressChallenges());
        result.put("inProgressGames", sosaStatus.inProgressGames());

        result.put("challengeJobEnabled", schedulerStatus.challengeJobEnabled());
        result.put("challengeJobLastExecution", schedulerStatus.challengeJobLastExecution());

        return result;
    }

}
