package net.chesstango.sosa.master;

import chariot.model.StatsPerf;
import chariot.model.StatsPerfType;
import chariot.model.UserAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyRequests;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState {
    private final Set<String> availableWorkers = new HashSet<>();

    @Setter
    @Getter
    private UserAuth myProfile;

    public synchronized void addAvailableWorker(String workerId) {
        if (availableWorkers.add(workerId)) {
            log.info("New worker available: {}. Total workers: {} ", workerId, availableWorkers.size());
        }
    }

    public synchronized void removeAvailableWorker(String workerId) {
        if (availableWorkers.remove(workerId)) {
            log.info("Removing worker: {}. Total worker available: {}",workerId,  availableWorkers.size());
        }
    }

    public synchronized boolean thereIsAvailableWorker() {
        return !availableWorkers.isEmpty();
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
