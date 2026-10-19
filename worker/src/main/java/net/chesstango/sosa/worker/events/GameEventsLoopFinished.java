package net.chesstango.sosa.worker.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */

public class GameEventsLoopFinished extends ApplicationEvent {
    public GameEventsLoopFinished(Object source) {
        super(source);
    }
}
