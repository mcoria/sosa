package net.chesstango.sosa.master.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mauricio Coria
 */
@RestController
@RequestMapping("/status") //
public class StatusController {


    @GetMapping
    public String get() {
        return "Hello, Spring Boot 4!";
    }

}
