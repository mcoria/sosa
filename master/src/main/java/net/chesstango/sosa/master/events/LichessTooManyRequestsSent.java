package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessTooManyRequestsSent extends ApplicationEvent {
    public LichessTooManyRequestsSent(Object source) {
        super(source);
    }
}
