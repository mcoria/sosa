package net.chesstango.sosa.messages.master;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkerBusy {
    private final String workerId;
}
