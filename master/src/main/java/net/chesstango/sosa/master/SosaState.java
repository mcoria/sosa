package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.ChallengeEvent;
import net.chesstango.sosa.master.events.GameEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class SosaState implements ApplicationListener<SosaEvent> {

    private final CircularFifoQueue<String> createdGames = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> finishedGames = new CircularFifoQueue<>();

    private final CircularFifoQueue<String> createdChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> acceptedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> declinedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> canceledChallenges = new CircularFifoQueue<>();


    @Override
    public synchronized void onApplicationEvent(SosaEvent event) {
        if (event instanceof GameEvent gameEvent) {
            switch (gameEvent.getType()) {
                case GAME_STARED -> createdGames.add(gameEvent.getGameId());
                case GAME_FINISHED -> finishedGames.add(gameEvent.getGameId());
                default -> {
                    log.warn("Unknown game event type: {}", gameEvent.getType());
                }
            }
        } else if (event instanceof ChallengeEvent challengeEvent) {
            switch (challengeEvent.getType()) {
                case CHALLENGE_CREATED -> createdChallenges.add(challengeEvent.getChallengeId());
                case CHALLENGE_ACCEPTED -> acceptedChallenges.add(challengeEvent.getChallengeId());
                case CHALLENGE_DECLINED -> declinedChallenges.add(challengeEvent.getChallengeId());
                case CHALLENGE_CANCELLED -> canceledChallenges.add(challengeEvent.getChallengeId());
                default -> {
                    log.warn("Unknown challenge event type: {}", challengeEvent.getType());
                }
            }
        }
    }

    public synchronized boolean isBusy() {
        return isChallengeInProgress() || isGameInProgress();
    }

    private boolean isGameInProgress() {
        Set<String> onGoingGamesSet = new HashSet<>(createdGames);
        onGoingGamesSet.removeAll(finishedGames);
        return !onGoingGamesSet.isEmpty();
    }

    private boolean isChallengeInProgress() {
        Set<String> onGoingChallengesSet = new HashSet<>(acceptedChallenges);
        onGoingChallengesSet.removeAll(declinedChallenges);
        onGoingChallengesSet.removeAll(canceledChallenges);
        return !onGoingChallengesSet.isEmpty();
    }

}
