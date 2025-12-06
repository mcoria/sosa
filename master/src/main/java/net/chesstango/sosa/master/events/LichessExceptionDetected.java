package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessExceptionDetected extends ApplicationEvent {
    public LichessExceptionDetected(Object source) {
        super(source);
    }
}
