package net.chesstango.sosa.master.lichess;

/**
 * @author Mauricio Coria
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException() {
        super("Too many requests");
    }
}
