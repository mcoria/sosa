package net.chesstango.sosa.master.lichess;

import chariot.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class ChallengeHandler {
    private final LichessClient client;

    private final SosaState sosaState;

    private volatile boolean acceptChallenges;

    public ChallengeHandler(LichessClient client,
                            SosaState sosaState) {
        this.client = client;
        this.sosaState = sosaState;
    }

    @PostConstruct
    public void init() {
        startAcceptingChallenges();
    }

    public void handleChallengeEvent(Event event) {
        switch (event.type()) {
            case challenge -> challengeCreated((Event.ChallengeCreatedEvent) event);
            case challengeCanceled -> challengeCanceled((Event.ChallengeCanceledEvent) event);
            case challengeDeclined -> challengeDeclined((Event.ChallengeDeclinedEvent) event);
        }
    }

    @EventListener
    public void onLichessTooManyExpired(LichessTooManyExpired lichessTooManyExpired) {
        switch (lichessTooManyExpired.getExpirationType()) {
            case GAMES:
                startAcceptingChallenges();
                break;
            case REQUESTS:
                break;
            default:
                break;
        }
    }

    @EventListener(LichessTooManyGamesPlayed.class)
    public void onLichessTooManyGamesPlayed() {
        stopAcceptingChallenges();
    }

    void challengeCreated(Event.ChallengeCreatedEvent event) {
        log.info("[{}] ChallengeCreatedEvent", event.id());

        if (isMyChallenge(event)) {
            log.info("[{}] My challenge, ignoring", event.id());
            return;
        }

        if (acceptChallenges) {
            Optional<ChallengeInfo.Player> challengerPlayerOpt = event
                    .challenge()
                    .players()
                    .challengerOpt();

            if (challengerPlayerOpt.isEmpty()) {
                log.warn("[{}] Challenge has no challenger", event.id());
                sendDeclineChallenge(event);
                return;
            }

            ChallengeInfo.Player challengerPlayer = challengerPlayerOpt.get();

            if (isChallengeAcceptable(event, challengerPlayer)) {
                boolean isHuman = isChallengerHuman(challengerPlayer);
                if (sosaState.isChallengeAcceptable(isHuman)) {
                    log.info("[{}] Challenge acceptable", event.id());
                    sendAcceptChallenge(event);
                    sosaState.addChallenge(event.id());
                } else {
                    log.info("[{}] There is no available worker to accept this challenge", event.id());
                    sendDeclineChallenge(event);
                }
            } else {
                log.info("[{}] Challenge not acceptable", event.id());
                sendDeclineChallenge(event);
            }
        } else {
            log.info("[{}] Not accepting more challenges at this time", event.id());
            sendDeclineChallenge(event);
        }
    }

    void challengeCanceled(Event.ChallengeCanceledEvent event) {
        log.info("[{}] ChallengeCanceledEvent", event.id());
        sosaState.removeChallenge(event.id());
    }

    void challengeDeclined(Event.ChallengeDeclinedEvent event) {
        log.info("[{}] ChallengeDeclinedEvent", event.id());
        sosaState.removeChallenge(event.id());
    }

    void stopAcceptingChallenges() {
        log.info("Accepting challenges disabled");
        this.acceptChallenges = false;
    }

    void startAcceptingChallenges() {
        log.info("Accepting challenges enabled");
        this.acceptChallenges = true;
    }


    /**
     * PRIVATE METHODS
     *
     */
    private void sendAcceptChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Accepting challenge", event.id());
        client.challengeAccept(event.id());
    }

    private void sendDeclineChallenge(Event.ChallengeEvent event) {
        log.info("[{}] Declining challenge", event.id());
        client.challengeDecline(event.id());
    }

    // Siempre acepto mis propios challenges
    private boolean isMyChallenge(Event.ChallengeEvent event) {
        ChallengeInfo.Players players = event.challenge().players();

        return switch (players) {
            case ChallengeInfo.FromTo fromTo -> fromTo.challenger().user().id().equals(sosaState.getMyProfile().id());
            case ChallengeInfo.From from -> from.challenger().user().id().equals(sosaState.getMyProfile().id());
            default -> false;
        };
    }

    private boolean isChallengeAcceptable(Event.ChallengeEvent event, ChallengeInfo.Player challengerPlayer) {
        GameType gameType = event.challenge().gameType();

        return isVariantAcceptable(gameType.variant())                          // Chess variant
                && isTimeControlAcceptable(gameType.timeControl())              // Time control
                && (isChallengerHuman(challengerPlayer) || isChallengerAcceptable(challengerPlayer, gameType.timeControl().speed()));
    }

    private boolean isChallengerAcceptable(ChallengeInfo.Player challengerPlayer, Enums.Speed speed) {
        StatsPerfType statsPerfType = switch (speed) {
            case bullet -> StatsPerfType.bullet;
            case blitz -> StatsPerfType.blitz;
            case rapid -> StatsPerfType.rapid;
            default -> null;
        };

        if (statsPerfType == null) {
            return false;
        }

        int myRating = sosaState.getMyRating(statsPerfType);

        return challengerPlayer.rating() <= myRating + ChallengerBot.RATING_THRESHOLD;
    }

    public static boolean isVariantAcceptable(Variant variant) {
        return Variant.Basic.standard.equals(variant) || variant instanceof Variant.FromPosition;
    }

    public static boolean isTimeControlAcceptable(TimeControl timeControl) {
        Predicate<RealTime> supportedRealtimeGames = realtime ->
                (Enums.Speed.bullet.equals(realtime.speed())
                        || Enums.Speed.blitz.equals(realtime.speed())
                        || Enums.Speed.rapid.equals(realtime.speed()))
                        && realtime.initial().getSeconds() >= 30L;


        return //timeControl instanceof Unlimited ||                                                   // Unlimited games x el momento no soportados
                (timeControl instanceof RealTime realtime && supportedRealtimeGames.test(realtime));   // Realtime

    }

    public static boolean isChallengerHuman(ChallengeInfo.Player challengerPlayer) {
        if (challengerPlayer.user().titleOpt().isEmpty()) { // Quiere decir que es human - aceptamos en todos los casos
            log.info("Challenger {} is human", challengerPlayer.user().id());
            return true;
        }
        return false;
    }
}
