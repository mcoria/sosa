package net.chesstango.sosa.master;

import chariot.model.StatsPerf;
import chariot.model.StatsPerfType;
import chariot.model.UserAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState {
    private final Queue<String> availableWorkers = new LinkedList<>();

    @Setter
    @Getter
    private UserAuth myProfile;

    public synchronized boolean thereAreAvailableWorkers() {
        return !availableWorkers.isEmpty();
    }

    public synchronized void addAvailableWorker(String workerId) {
        log.info("Worker available: {}", workerId);
        if (!availableWorkers.contains(workerId)) {
            availableWorkers.add(workerId);
        }
    }

    public synchronized boolean isAvailableWorker(String workerId) {
        return availableWorkers.contains(workerId);
    }

    public synchronized Optional<String> pollAvailableWorker() {
        String workerId = availableWorkers.poll();
        return Optional.ofNullable(workerId);
    }

    public synchronized int getRating(StatsPerfType type) {
        Map<StatsPerfType, StatsPerf> rating = myProfile
                .ratings();
        StatsPerf stats = rating.get(type);
        if (stats instanceof StatsPerf.StatsPerfGame statsPerfGame) {
            return statsPerfGame.rating();
        }
        throw new RuntimeException("Rating not found");
    }
}
