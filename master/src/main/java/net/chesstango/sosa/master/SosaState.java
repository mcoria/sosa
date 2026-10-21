package net.chesstango.sosa.master;

import chariot.model.StatsPerf;
import chariot.model.StatsPerfType;
import chariot.model.UserAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.Challenger;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState {
    private final Challenger challenger;

    private final ChallengeExpire challengeExpire;

    private final Set<Worker> availableWorkers;

    private final Set<Challenge> inProgressChallenges;

    private final Set<String> inProgressGames;

    @Setter
    @Getter
    private UserAuth myProfile;

    public SosaState(Challenger challenger,
                     ChallengeExpire challengeExpire) {
        this.challenger = challenger;
        this.challengeExpire = challengeExpire;
        this.availableWorkers = new HashSet<>();
        this.inProgressChallenges = new HashSet<>();
        this.inProgressGames = new HashSet<>();
    }

    public synchronized void addAvailableWorker(String workerId) {
        Optional<Worker> workerOpt = availableWorkers.stream()
                .filter(worker -> worker.getWorkerId().equals(workerId))
                .findFirst();

        Worker worker = workerOpt.orElse(null);

        if (worker == null) {

            worker = new Worker(workerId);

            availableWorkers.add(worker);

            log.info("New worker available: {}. Total workers: {} ", workerId, availableWorkers.size());
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

    public synchronized void addChallenge(String challengeId) {
        inProgressChallenges.add(new Challenge(challengeId));
    }

    public synchronized void removeChallenge(String challengeId) {
        inProgressChallenges.removeIf(challenge -> challenge.getChallengeId().equals(challengeId));
    }

    public synchronized void addGame(String gameId) {
        removeChallenge(gameId);
        inProgressGames.add(gameId);
    }

    public synchronized void removeGame(String gameId) {
        inProgressGames.remove(gameId);
    }

    public synchronized boolean isInProgressGame(String gameId) {
        return inProgressGames.contains(gameId);
    }

    public synchronized boolean isChallengeAcceptable() {
        return availableWorkers.size() >= inProgressChallenges.size();
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

    public synchronized void expireWorker(int workerExpire) {
        log.info("Expiring workers");

        Instant now = Instant.now();
        availableWorkers.removeIf(worker -> {
            Duration duration = Duration.between(worker.getLastAvailable(), now);
            boolean remove = duration.toMinutes() >= workerExpire;
            if (remove) {
                log.info("Worker expired: {}", worker.getWorkerId());
            }
            return remove;
        });
    }

    public synchronized void expireChallenges(int challengeExpireSecs) {
        log.info("Expiring challenges");

        Instant now = Instant.now();

        Iterator<Challenge> iterator = inProgressChallenges.iterator();
        while (iterator.hasNext()) {
            Challenge challenge = iterator.next();
            Duration duration = Duration.between(challenge.getCreated(), now);
            if (duration.toMinutes() >= challengeExpireSecs) {
                log.info("Challenge expired: {}", challenge.getChallengeId());
                if (challengeExpire.expire(challenge.getChallengeId())) {
                    iterator.remove();
                }
            }
        }
    }

    public synchronized void challengeBot() {
        if (availableWorkers.size() > inProgressChallenges.size()) {
            Optional<chariot.model.Challenge> liChessChallengeOpt = challenger.challengeRandomBot(myProfile);
            liChessChallengeOpt.ifPresent(challenge -> addChallenge(challenge.id()));
        } else {
            log.info("No available workers to challenge or too many challenges in progress.");
        }
    }
}
