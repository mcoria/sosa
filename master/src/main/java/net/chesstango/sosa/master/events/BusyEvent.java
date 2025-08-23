package net.chesstango.sosa.master.events;

import lombok.Getter;

/**
 * @author Mauricio Coria
 */
public class BusyEvent extends SosaEvent {

    @Getter
    private final boolean isBusy;

    public BusyEvent(Object source, boolean isBusy) {
        super(source);
        this.isBusy = isBusy;
    }
}
