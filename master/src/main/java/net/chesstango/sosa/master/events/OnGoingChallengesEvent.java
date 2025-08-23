package net.chesstango.sosa.master.events;

import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class OnGoingChallengesEvent extends SosaEvent {

    @Getter
    private final boolean onGoing;

    public OnGoingChallengesEvent(Object source, boolean onGoing) {
        super(source);
        this.onGoing = onGoing;
    }
}
