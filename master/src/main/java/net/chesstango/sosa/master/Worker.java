package net.chesstango.sosa.master;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Mauricio Coria
 */
public class Worker {
    @Getter
    private final String workerId;

    @Getter
    @Setter
    private Instant lastAvailable;

    public Worker(String workerId) {
        this.workerId = workerId;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Worker worker)) return false;

        return Objects.equals(workerId, worker.workerId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(workerId);
    }
}
