package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public abstract class SosaEvent extends ApplicationEvent {
    public SosaEvent(Object source) {
        super(source);
    }
}
