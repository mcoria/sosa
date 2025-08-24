package net.chesstango.sosa.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.ChallengeEvent;
import net.chesstango.sosa.master.events.GameEvent;
import net.chesstango.sosa.master.events.SosaEvent;
import net.chesstango.sosa.master.lichess.LichessGame;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static net.chesstango.sosa.master.configs.AsyncConfig.GAME_TASK_EXECUTOR;

@Component
@Slf4j
public class SosaState implements ApplicationListener<SosaEvent> {

    private final Executor gameTaskExecutor;
    private final Map<String, LichessGame> activeGames;

    private final CircularFifoQueue<String> createdGames = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> finishedGames = new CircularFifoQueue<>();

    private final CircularFifoQueue<String> createdChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> acceptedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> declinedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> canceledChallenges = new CircularFifoQueue<>();

    public SosaState(@Qualifier(GAME_TASK_EXECUTOR) Executor gameTaskExecutor,
                     Map<String, LichessGame> activeGames) {
        this.gameTaskExecutor = gameTaskExecutor;
        this.activeGames = activeGames;
    }

    @Override
    public synchronized void onApplicationEvent(SosaEvent event) {
        if (event instanceof GameEvent gameEvent) {
            switch (gameEvent.getType()) {
                case GAME_STARED -> {
                    createdGames.add(gameEvent.getGameId());
                    createdChallenges.remove(gameEvent.getGameId());
                    acceptedChallenges.remove(gameEvent.getGameId());
                    declinedChallenges.remove(gameEvent.getGameId());
                    canceledChallenges.remove(gameEvent.getGameId());

                    startGame(gameEvent.getGameId());
                }
                case GAME_FINISHED -> {
                    finishedGames.add(gameEvent.getGameId());
                }
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
        return thereIsChallengeInProgress(Optional.empty()) || isGameInProgress();
    }

    public synchronized boolean isGameInProgress() {
        Set<String> onGoingGamesSet = new HashSet<>(createdGames);
        onGoingGamesSet.removeAll(finishedGames);
        return !onGoingGamesSet.isEmpty();
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

    private void startGame(String gameId) {
        LichessGame lichessGame = activeGames.get(gameId);
        gameTaskExecutor.execute(lichessGame);
    }
}
