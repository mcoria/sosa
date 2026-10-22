package net.chesstango.sosa.messages.master;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author Mauricio Coria
 */
@Data
@AllArgsConstructor
public class SendAbort {
    private final String gameId;
}
