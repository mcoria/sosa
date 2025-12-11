package net.chesstango.sosa.master.events;

import lombok.Getter;
import net.chesstango.sosa.master.lichess.errors.RetryIn;
import org.springframework.context.ApplicationEvent;

/**
 * @author Mauricio Coria
 */
@Getter
public class LichessTooManyGames extends ApplicationEvent {
    private final RetryIn retryIn;

    public LichessTooManyGames(Object source, RetryIn retryIn) {
        super(source);
        this.retryIn = retryIn;
    }
}
