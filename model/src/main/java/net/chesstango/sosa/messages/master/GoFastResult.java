package net.chesstango.sosa.messages.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mauricio Coria
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoFastResult {
    private String gameId;
    private String move;
}
