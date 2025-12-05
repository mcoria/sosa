package net.chesstango.sosa.master.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessConnected extends ApplicationEvent {
    public LichessConnected(Object source) {
        super(source);
    }
}
