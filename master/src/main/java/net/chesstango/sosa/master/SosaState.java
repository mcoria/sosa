package net.chesstango.sosa.master;

import chariot.model.StatsPerf;
import chariot.model.StatsPerfType;
import chariot.model.UserAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState {
    private final Set<Worker> availableWorkers = new HashSet<>();

    @Setter
    @Getter
    private UserAuth myProfile;

    public synchronized void addAvailableWorker(String workerId) {
        Optional<Worker> workerOpt = availableWorkers.stream()
                .filter(worker -> worker.getWorkerId().equals(workerId))
                .findFirst();

        Worker worker = workerOpt.orElse(null);

        if (worker == null) {
            log.info("New worker available: {}. Total workers: {} ", workerId, availableWorkers.size());

            worker = new Worker(workerId);

            availableWorkers.add(worker);
        }

        worker.setLastAvailable(Instant.now());
    }

    public synchronized void removeAvailableWorker(String workerId) {
        availableWorkers.removeIf(worker -> worker.getWorkerId().equals(workerId));
        log.info("Removed worker: {}. Total worker available: {}", workerId, availableWorkers.size());
    }

    public synchronized boolean thereIsAvailableWorker() {
        return !availableWorkers.isEmpty();
    }

    public synchronized int getMyRating(StatsPerfType type) {
        Map<StatsPerfType, StatsPerf> rating = myProfile
                .ratings();
        StatsPerf stats = rating.get(type);
        if (stats instanceof StatsPerf.StatsPerfGame statsPerfGame) {
            return statsPerfGame.rating();
        }
        throw new RuntimeException("Rating not found");
    }

    public synchronized void expireWorker() {
        log.info("Expiring workers");

        Instant now = Instant.now();
        availableWorkers.removeIf(worker -> {
            Duration duration = Duration.between(worker.getLastAvailable(), now);
            boolean remove = duration.toMinutes() > 5;
            if (remove) {
                log.info("Worker expired: {}", worker.getWorkerId());
            }
            return remove;
        });
    }
}
