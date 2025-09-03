package net.chesstango.sosa.messages.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Mauricio Coria
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoFast {
    private String gameId;
    private int wTime;
    private int bTime;
    private int wInc;
    private int bInc;
    private List<String> moves;
}
