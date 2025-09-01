package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.ChallengeEvent;
import net.chesstango.sosa.master.events.GameFinishEvent;
import net.chesstango.sosa.master.events.GameStartEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class SosaState implements ApplicationListener<SosaEvent> {

    private final Set<String> availableWorkers = new HashSet<>();

    private final CircularFifoQueue<String> createdGames = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> finishedGames = new CircularFifoQueue<>();

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
                createdGames.add(gameStartEvent.getGameId());
                createdChallenges.remove(gameStartEvent.getGameId());
                acceptedChallenges.remove(gameStartEvent.getGameId());
                declinedChallenges.remove(gameStartEvent.getGameId());
                canceledChallenges.remove(gameStartEvent.getGameId());
            }
            case GameFinishEvent gameFinishEvent -> {
                finishedGames.add(gameFinishEvent.getGameId());
            }
            default -> {
            }
        }
    }

    public synchronized boolean isBusy() {
        return thereIsChallengeInProgress(Optional.empty()) || !thereAreAvailableWorkers();
    }

    public synchronized boolean thereAreAvailableWorkers() {
        Set<String> onGoingGamesSet = new HashSet<>(createdGames);
        onGoingGamesSet.removeAll(finishedGames);
        return onGoingGamesSet.size() < availableWorkers.size();
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

    public synchronized boolean isChallengePending(String challengeId) {
        Set<String> onGoingChallengesSet = new HashSet<>(createdChallenges);

        onGoingChallengesSet.addAll(acceptedChallenges);

        onGoingChallengesSet.removeAll(declinedChallenges);
        onGoingChallengesSet.removeAll(canceledChallenges);

        return onGoingChallengesSet.contains(challengeId);
    }

    public synchronized void increaseWorkerSet(String workerId) {
        if (availableWorkers.add(workerId)) {
            log.info("Worker {} registered", workerId);
        }
    }

    public synchronized void decreaseWorkerSet(String workerId) {
        if (availableWorkers.remove(workerId)) {
            log.info("Worker {} unregistered", workerId);
        }
    }
}
