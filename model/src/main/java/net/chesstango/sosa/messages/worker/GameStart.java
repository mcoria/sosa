package net.chesstango.sosa.messages.worker;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author Mauricio Coria
 */
@Data
@AllArgsConstructor
public class GameStart {
    private final String gameId;
    private final String color;
}
