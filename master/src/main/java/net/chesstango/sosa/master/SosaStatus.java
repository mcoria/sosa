package net.chesstango.sosa.master;

import java.util.Collection;

/**
 * @author Mauricio Coria
 */
public record SosaStatus(Collection<Worker> availableWorkers,

                         Collection<Challenge> inProgressChallenges,

                         Collection<String> inProgressGames) {
}
