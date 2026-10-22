package net.chesstango.sosa.master.rest;

import net.chesstango.sosa.master.SosaState;
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

    public StatusController(SosaState sosaState) {
        this.sosaState = sosaState;
    }


    @GetMapping
    public Map<String, Object> get() {
        Map<String, Object> result = new HashMap<>();
        result.put("sosaStatus", sosaState.getSosaStatus());
        result.put("jobEnabled", false);
        return result;
    }

}
