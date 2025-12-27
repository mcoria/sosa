package net.chesstango.sosa.messages.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mauricio Coria
 */

@Data
@AllArgsConstructor
public class SendMove {
    private final String gameId;
    private final String move;
}
