package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.ChallengeEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState implements ApplicationListener<SosaEvent> {

    private final BlockingQueue<String> availableWorkers = new LinkedBlockingDeque<>();

    private final CircularFifoQueue<String> createdChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> acceptedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> declinedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> canceledChallenges = new CircularFifoQueue<>();


    @Override
    public synchronized void onApplicationEvent(SosaEvent event) {
        switch (event) {
            case ChallengeEvent challengeEvent -> {
                switch (challengeEvent.getType()) {
                    case CHALLENGE_CREATED -> createdChallenges.add(challengeEvent.getChallengeId());
                    case CHALLENGE_ACCEPTED -> acceptedChallenges.add(challengeEvent.getChallengeId());
                    case CHALLENGE_DECLINED -> declinedChallenges.add(challengeEvent.getChallengeId());
                    case CHALLENGE_CANCELLED -> canceledChallenges.add(challengeEvent.getChallengeId());
                    default -> log.warn("Unknown challenge event type: {}", challengeEvent.getType());
                }
            }
            case GameStartEvent gameStartEvent -> {
                createdChallenges.remove(gameStartEvent.getGameId());
                acceptedChallenges.remove(gameStartEvent.getGameId());
                declinedChallenges.remove(gameStartEvent.getGameId());
                canceledChallenges.remove(gameStartEvent.getGameId());
            }
            default -> {
            }
        }
    }

    public synchronized boolean isBusy() {
        return thereIsChallengeInProgress(Optional.empty()) || !thereAreAvailableWorkers();
    }

    public synchronized boolean thereIsChallengeInProgress(Optional<String> excludedChallengeId) {
        Set<String> onGoingChallengesSet = new HashSet<>(createdChallenges);

        onGoingChallengesSet.addAll(acceptedChallenges);

        onGoingChallengesSet.removeAll(declinedChallenges);
        onGoingChallengesSet.removeAll(canceledChallenges);

        // Remove excluded challenge if present
        excludedChallengeId.ifPresent(onGoingChallengesSet::remove);

        return !onGoingChallengesSet.isEmpty();
    }

    public synchronized boolean thereAreAvailableWorkers() {
        return !availableWorkers.isEmpty();
    }

    public synchronized boolean isChallengePending(String challengeId) {
        Set<String> onGoingChallengesSet = new HashSet<>(createdChallenges);

        onGoingChallengesSet.addAll(acceptedChallenges);

        onGoingChallengesSet.removeAll(declinedChallenges);
        onGoingChallengesSet.removeAll(canceledChallenges);

        return onGoingChallengesSet.contains(challengeId);
    }

    public void addAvailableWorker(String workerId) {
        try {
            availableWorkers.put(workerId);
            log.info("Worker available: {}", workerId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNextWorker(String gameId) {
        try {
            String workerId = availableWorkers.take();
            log.info("[{}] Worker {} assigned to game", gameId, workerId);
            return workerId;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
