package net.chesstango.sosa.messages.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Mauricio Coria
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStart {
    private String gameId;
    private String workerId;
    private String color;
}
