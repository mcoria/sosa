package net.chesstango.sosa.master.rest;

import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.SosaStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public SosaStatus get() {
        return sosaState.getSosaStatus();
    }

}
