package net.chesstango.sosa.master.rest;

import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.scheduler.QueryScheduler;
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
        result.put("sosaStatus", sosaState.getStatus());
        result.put("jobEnabled", queryScheduler.getStatus());
        return result;
    }

}
