package net.chesstango.sosa.master.lichess.errors;

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
