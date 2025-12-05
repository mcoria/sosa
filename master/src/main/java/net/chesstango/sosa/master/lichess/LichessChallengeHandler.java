package net.chesstango.sosa.master.lichess;

import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.ChallengeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessChallengeHandler {
    private final LichessClient client;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SosaState sosaState;

    private boolean acceptChallenges;

    public LichessChallengeHandler(LichessClient client, SosaState sosaState, ApplicationEventPublisher applicationEventPublisher) {
        this.client = client;
        this.applicationEventPublisher = applicationEventPublisher;
        this.sosaState = sosaState;
        this.acceptChallenges = true;
    }

    public void handleChallengeEvent(Event event) {
        switch (event.type()) {
            case challenge -> challengeCreated((Event.ChallengeCreatedEvent) event);
            case challengeCanceled -> challengeCanceled((Event.ChallengeCanceledEvent) event);
            case challengeDeclined -> challengeDeclined((Event.ChallengeDeclinedEvent) event);
        }
    }

    public void challengeCreated(Event.ChallengeCreatedEvent event) {
        log.info("[{}] ChallengeCreatedEvent", event.id());
        if (acceptChallenges) {
            if (sosaState.thereAreAvailableWorkers()) {
                if (isMyChallenge(event) || isChallengeAcceptable(event)) {
                    sendAcceptChallenge(event);
                } else {
                    sendDeclineChallenge(event);
                }
            } else {
                log.info("[{}] There is no available worker to accept this challenge", event.id());
                sendDeclineChallenge(event);
            }
        } else {
            log.info("[{}] Not accepting more challenges at this time", event.id());
            sendDeclineChallenge(event);
        }
    }

    public void challengeCanceled(Event.ChallengeCanceledEvent event) {
        log.info("[{}] ChallengeCanceledEvent", event.id());
        applicationEventPublisher.publishEvent(new ChallengeEvent(this, ChallengeEvent.Type.CHALLENGE_CANCELLED, event.id()));
    }

    public void challengeDeclined(Event.ChallengeDeclinedEvent event) {
        log.info("[{}] ChallengeDeclinedEvent", event.id());
        applicationEventPublisher.publishEvent(new ChallengeEvent(this, ChallengeEvent.Type.CHALLENGE_DECLINED, event.id()));
    }

    public void stopAcceptingChallenges() {
        this.acceptChallenges = false;
    }


    /**
     * PRIVATE METHODS
     *
     */
    private void sendAcceptChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Accepting challenge", event.id());
        client.challengeAccept(event.id());
        applicationEventPublisher.publishEvent(new ChallengeEvent(this, ChallengeEvent.Type.CHALLENGE_ACCEPTED, event.id()));
    }

    private void sendDeclineChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Declining challenge", event.id());
        client.challengeDecline(event.id());
        applicationEventPublisher.publishEvent(new ChallengeEvent(this, ChallengeEvent.Type.CHALLENGE_DECLINED, event.id()));
    }

    // Siempre acepto mis propios challenges
    private boolean isMyChallenge(Event.ChallengeEvent event) {
        Optional<ChallengeInfo.Player> challengerPlayer = event.challenge().players().challengerOpt();

        return Objects.equals(challengerPlayer.get().user().id(), sosaState.getMyProfile().id());
    }

    private boolean isChallengeAcceptable(Event.ChallengeEvent event) {
        Optional<ChallengeInfo.Player> challengerPlayer = event
                .challenge()
                .players()
                .challengerOpt();

        if (challengerPlayer.isEmpty()) {
            log.warn("Challenge has no challenger");
            return false;
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

        int myRating = sosaState.getRating(statsPerfType);

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
