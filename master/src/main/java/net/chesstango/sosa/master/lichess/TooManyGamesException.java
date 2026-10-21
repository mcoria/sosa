package net.chesstango.sosa.master.lichess;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Mauricio Coria
 */
public class TooManyGamesException extends RuntimeException {
    @Getter
    private final RetryIn retryIn;

    public TooManyGamesException(RetryIn retryIn) {
        super("Too many games");
        this.retryIn = retryIn;
    }
}
