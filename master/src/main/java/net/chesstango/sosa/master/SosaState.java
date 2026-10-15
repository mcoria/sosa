package net.chesstango.sosa.master;

import chariot.model.StatsPerf;
import chariot.model.StatsPerfType;
import chariot.model.UserAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.Challenger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean sendRequestsAllowed;

    private final AtomicBoolean sendChallengesAllowed;

    @Setter
    @Getter
    private UserAuth myProfile;

    public SosaState(Challenger challenger,
                     ChallengeExpire challengeExpire) {
        this.challenger = challenger;
        this.challengeExpire = challengeExpire;
        this.availableWorkers = new HashSet<>();
        this.inProgressChallenges = new HashSet<>();
        this.sendChallengesAllowed = new AtomicBoolean(true);
        this.sendRequestsAllowed = new AtomicBoolean(true);
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

    public synchronized void expireChallenges() {
        log.info("Expiring challenges");

        Instant now = Instant.now();

        Iterator<Challenge> iterator = inProgressChallenges.iterator();
        while (iterator.hasNext()) {
            Challenge challenge = iterator.next();
            Duration duration = Duration.between(challenge.getCreated(), now);
            if (duration.toMinutes() > 3) {
                log.info("Challenge expired: {}", challenge.getChallengeId());
                if (challengeExpire.expire(challenge.getChallengeId())) {
                    iterator.remove();
                }
            }
        }
    }

    public synchronized void challengeBot() {
        try {
            if (canSendRequests()) {
                if (availableWorkers.size() > inProgressChallenges.size()) {
                    Optional<chariot.model.Challenge> liChessChallengeOpt = challenger.challengeRandomBot(myProfile);
                    liChessChallengeOpt.ifPresent(challenge -> addChallenge(challenge.id()));
                } else {
                    log.info("No available workers to challenge or too many challenges in progress.");
                }
            } else {
                log.warn("Can not challenge at this time.");
            }
        } catch (RuntimeException e) {
            log.error("Error challenging random bot", e);
        }
    }

    @EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequests() {
        log.warn("Lichess API: too many requests. Stop sending requests to lichess.");
        sendRequestsAllowed.set(false);
    }

    @EventListener(LichessTooManyGamesPlayed.class)
    public void onLichessTooManyGames() {
        log.warn("Lichess API: too many games. Stop sending challenges to lichess.");
        sendChallengesAllowed.set(false);
    }

    @EventListener
    public void onLichessTooManyExpired(LichessTooManyExpired lichessTooManyExpired) {
        switch (lichessTooManyExpired.getExpirationType()) {
            case GAMES:
                log.info("Sending challenges again.");
                sendChallengesAllowed.set(true);
                break;
            case REQUESTS:
                log.warn("Sending requests again.");
                sendRequestsAllowed.set(true);
                break;
        }
    }

    boolean canSendRequests() {
        return sendRequestsAllowed.get() && sendChallengesAllowed.get();
    }
}
