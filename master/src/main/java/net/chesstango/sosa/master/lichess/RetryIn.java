package net.chesstango.sosa.master.lichess;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author Mauricio Coria
 */
@AllArgsConstructor
@Getter
public class RetryIn {
    final long seconds;
}
