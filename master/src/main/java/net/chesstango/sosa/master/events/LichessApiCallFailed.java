package net.chesstango.sosa.master.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
public class LichessApiCallFailed extends ApplicationEvent {

    @Getter
    private final String failMessage;

    public LichessApiCallFailed(Object source, String failMessage) {
        super(source);
        this.failMessage = failMessage;
    }
}
