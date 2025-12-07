package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessApiTooManyRequests extends ApplicationEvent {

    public LichessApiTooManyRequests(Object source) {
        super(source);
    }
}
