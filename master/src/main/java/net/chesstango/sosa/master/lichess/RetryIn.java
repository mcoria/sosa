package net.chesstango.sosa.master.lichess;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Mauricio Coria
 */
@Data
@AllArgsConstructor
public class RetryIn {
    final long seconds;
}
