package net.chesstango.sosa.master.events;

import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class ChallengeEvent extends SosaEvent {
    public enum Type {
        CHALLENGE_CREATED,
        CHALLENGE_ACCEPTED,
        CHALLENGE_DECLINED,
        CHALLENGE_CANCELLED
    }

    @Getter
    private final String challengeId;

    @Getter
    private final Type type;

    public ChallengeEvent(Object source, Type type, String challengeId) {
        super(source);
        this.challengeId = challengeId;
        this.type = type;
    }
}
