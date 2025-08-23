package net.chesstango.sosa.master.lichess;

import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.BusyEvent;
import net.chesstango.sosa.master.events.OnGoingChallengesEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessChallengeHandler implements ApplicationListener<BusyEvent> {
    private final LichessClient client;

    private final ApplicationEventPublisher applicationEventPublisher;

    private boolean acceptChallenges;

    private final CircularFifoQueue<String> acceptedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> declinedChallenges = new CircularFifoQueue<>();
    private final CircularFifoQueue<String> canceledChallenges = new CircularFifoQueue<>();

    private final AtomicBoolean isBusy = new AtomicBoolean(false);
    private final AtomicBoolean onGoingChallenges = new AtomicBoolean(false);

    public LichessChallengeHandler(LichessClient client, ApplicationEventPublisher applicationEventPublisher) {
        this.client = client;
        this.applicationEventPublisher = applicationEventPublisher;
        this.acceptChallenges = true;
    }

    @Override
    public void onApplicationEvent(BusyEvent event) {
        isBusy.set(event.isBusy());
    }

    public void handleChallengeEvent(Event event) {
        switch (event.type()) {
            case challenge -> challengeCreated((Event.ChallengeCreatedEvent) event);
            case challengeCanceled -> challengeCanceled((Event.ChallengeCanceledEvent) event);
            case challengeDeclined -> challengeDeclined((Event.ChallengeDeclinedEvent) event);
        }

        Set<String> onGoingChallengesSet = new HashSet<>(acceptedChallenges);
        onGoingChallengesSet.removeAll(declinedChallenges);
        onGoingChallengesSet.removeAll(canceledChallenges);

        boolean onGoingChallengesCurrent = !onGoingChallengesSet.isEmpty();
        if (onGoingChallengesCurrent != onGoingChallenges.get()) {
            log.info("OnGoingChallenges changed: {}", onGoingChallengesCurrent);
            onGoingChallenges.set(onGoingChallengesCurrent);
            applicationEventPublisher.publishEvent(new OnGoingChallengesEvent(this, onGoingChallengesCurrent));
        }

    }

    public void challengeCreated(Event.ChallengeCreatedEvent event) {
        log.info("[{}] ChallengeCreatedEvent", event.id());
        if (acceptChallenges) {
            if (!isBusy.get()) {
                if (declinedChallenges.contains(event.id())) {
                    log.info("[{}] Challenge declined before", event.id());
                    declineChallenge(event);
                    return;
                }

                if (canceledChallenges.contains(event.id())) {
                    log.info("[{}] Challenge canceled before", event.id());
                    declineChallenge(event);
                    return;
                }

                if (isChallengeAcceptable(event)) {
                    acceptChallenge(event);
                } else {
                    declineChallenge(event);
                }
            } else {
                log.info("[{}] Busy at this time", event.id());
                declineChallenge(event);
            }
        } else {
            log.info("[{}] Not accepting more challenges at this time", event.id());
            declineChallenge(event);
        }
    }

    public void challengeCanceled(Event.ChallengeCanceledEvent event) {
        log.info("[{}] ChallengeCanceledEvent", event.id());
        canceledChallenges.add(event.id());
    }

    public void challengeDeclined(Event.ChallengeDeclinedEvent event) {
        log.info("[{}] ChallengeDeclinedEvent", event.id());
        declinedChallenges.add(event.id());
    }

    public void stopAcceptingChallenges() {
        this.acceptChallenges = false;
    }


    /**
     * PRIVATE METHODS
     *
     */

    private void acceptChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Accepting challenge", event.id());
        client.challengeAccept(event.id());
        acceptedChallenges.add(event.id());
    }

    private void declineChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Declining challenge", event.id());
        client.challengeDecline(event.id());
        declinedChallenges.add(event.id());
    }

    private boolean isChallengeAcceptable(Event.ChallengeEvent event) {
        Optional<ChallengeInfo.Player> challengerPlayer = event.challenge().players().challengerOpt();
        Optional<ChallengeInfo.Player> challengedPlayer = event.challenge().players().challengedOpt();

        if (challengerPlayer.isEmpty() || challengedPlayer.isEmpty()) {
            log.warn("[{}] Challenge has no challenger or challenged player", event.id());
            return false;
        }

        if (client.isMe(challengerPlayer.get().user())) { // Siempre acepto mis propios challenges
            return true;
        }

        GameType gameType = event.challenge().gameType();

        return isVariantAcceptable(gameType.variant())                          // Chess variant
                && isTimeControlAcceptable(gameType.timeControl())              // Time control
                && isChallengerAcceptable(challengerPlayer.get(), gameType.timeControl().speed());
    }

    private boolean isChallengerAcceptable(ChallengeInfo.Player player, Enums.Speed speed) {
        if (player.user().titleOpt().isEmpty()) { // Quiere decir que es human - aceptamos en todos los casos
            log.info("Challenger {} is human", player.user().id());
            return true;
        }

        String userTitle = player.user().titleOpt().get();

        StatsPerfType statsPerfType = switch (speed) {
            case bullet -> StatsPerfType.bullet;
            case blitz -> StatsPerfType.blitz;
            case rapid -> StatsPerfType.rapid;
            default -> null;
        };

        if (statsPerfType == null) {
            return false;
        }

        int myRating = client.getRating(statsPerfType);

        return "BOT".equals(userTitle) && player.rating() <= myRating + LichessChallengerBot.RATING_THRESHOLD;
    }

    private static boolean isVariantAcceptable(Variant variant) {
        return Variant.Basic.standard.equals(variant) || variant instanceof Variant.FromPosition;
    }

    private static boolean isTimeControlAcceptable(TimeControl timeControl) {
        Predicate<RealTime> supportedRealtimeGames = realtime ->
                (Enums.Speed.bullet.equals(realtime.speed())
                        || Enums.Speed.blitz.equals(realtime.speed())
                        || Enums.Speed.rapid.equals(realtime.speed()))
                        && realtime.initial().getSeconds() >= 30L;


        return //timeControl instanceof Unlimited ||                                                   // Unlimited games x el momento no soportados
                (timeControl instanceof RealTime realtime && supportedRealtimeGames.test(realtime));   // Realtime

    }
}
