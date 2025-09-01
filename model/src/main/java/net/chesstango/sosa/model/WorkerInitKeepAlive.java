package net.chesstango.sosa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * @author Mauricio Coria
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerInitKeepAlive {
    private String workerId;
}
