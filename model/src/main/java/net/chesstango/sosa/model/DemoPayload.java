package net.chesstango.sosa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Mauricio Coria
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoPayload {
    private String id;
    private String content;
}
