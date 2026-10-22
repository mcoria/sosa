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

    // Maps HTTP GET requests for "/api/hello"
    @GetMapping
    public String sayHello() {
        return "Hello, Spring Boot 4!";
    }

}
