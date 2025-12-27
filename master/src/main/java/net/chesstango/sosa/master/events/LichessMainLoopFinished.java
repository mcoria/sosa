package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessMainLoopFinished extends ApplicationEvent {
    public LichessMainLoopFinished(Object source) {
        super(source);
    }
}
