package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessTooManyRequests extends ApplicationEvent {
    public LichessTooManyRequests(Object source) {
        super(source);
    }
}
