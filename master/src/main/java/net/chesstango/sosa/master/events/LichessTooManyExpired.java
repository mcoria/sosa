package net.chesstango.sosa.master.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
@Getter
public class LichessTooManyExpired extends ApplicationEvent {
    public enum ExpirationType {
        REQUESTS,
        GAMES
    };

    private final ExpirationType expirationType;

    public LichessTooManyExpired(Object source, ExpirationType expirationType) {
        super(source);
        this.expirationType = expirationType;
    }
}
