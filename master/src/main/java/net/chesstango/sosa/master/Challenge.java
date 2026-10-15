package net.chesstango.sosa.master;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Mauricio Coria
 */
public class Challenge {
    @Getter
    private final String challengeId;

    @Getter
    private final Instant created;

    public Challenge(String challengeId) {
        this.challengeId = challengeId;
        this.created = Instant.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Challenge challenge)) return false;

        return Objects.equals(challengeId, challenge.challengeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(challengeId);
    }
}
