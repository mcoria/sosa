package net.chesstango.sosa.worker.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */

public class GameInProgress extends ApplicationEvent {
    public GameInProgress(Object source) {
        super(source);
    }
}
