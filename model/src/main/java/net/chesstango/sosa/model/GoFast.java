package net.chesstango.sosa.model;

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
    private int wTime;
    private int bTime;
    private int wInc;
    private int bInc;
    private List<String> moves;
}
