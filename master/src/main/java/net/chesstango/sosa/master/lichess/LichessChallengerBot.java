package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class LichessChallengerBot {
    public static final int RATING_THRESHOLD = 150;

    private static final Random rand = new Random();

    private final LichessClient client;

    private final SosaState sosaState;

    private final BotQueue botQueue;

    private final List<Challenger> challengerTypes;

    private final AtomicBoolean sendRequestsAllowed;
    private final AtomicBoolean sendChallengesAllowed;

    public LichessChallengerBot(LichessClient client,
                                SosaState sosaState,
                                BotQueue botQueue,
                                @Value("${app.challengeTypes}") List<String> challengeTypes) {
        this.client = client;
        this.botQueue = botQueue;
        this.sosaState = sosaState;
        this.sendChallengesAllowed = new AtomicBoolean(true);
        this.sendRequestsAllowed = new AtomicBoolean(true);
        this.challengerTypes = new ArrayList<>();

        challengeTypes.forEach(challengeType -> {
            switch (challengeType.toLowerCase()) {
                case "bullet" -> challengerTypes.add(new BulletChallengerBot());
                case "blitz" -> challengerTypes.add(new BlitzChallengerBot());
                case "rapid" -> challengerTypes.add(new RapidChallengerBot());
                default -> log.warn("Unknown challenge type: {}", challengeType);
            }
        });
    }

    public void updateRatings() {
        Map<StatsPerfType, StatsPerf> myRatings = sosaState.getMyProfile().ratings();

        challengerTypes.forEach(challenger -> {
            challenger.setMyRating(myRatings);
            log.info("Rating for {}: {}", challenger.statsPerfType, challenger.myRating);
        });
    }

    public Optional<Challenge> challengeRandomBot() {
        log.info("Challenging random bot");

        List<Challenger> challengerTypes = new ArrayList<>(this.challengerTypes);

        Collections.shuffle(challengerTypes, rand);

        int counter = 0;
        for (User bot = botQueue.pickBot(); bot != null; bot = botQueue.pickBot()) {
            final User theBot = bot;
            // Avoid Fail[status=400, info=Info[message={"error":"You cannot challenge yourself"}]]
            if (!Objects.equals(theBot.id(), sosaState.getMyProfile().id())) {
                log.info("Challenging bot {}", theBot.id());
                Optional<Challenge> challengeOpt = challengerTypes
                        .stream()
                        .filter(aChallenger -> sendRequestsAllowed.get() && sendChallengesAllowed.get())
                        .filter(aChallenger -> aChallenger.filter(theBot))
                        .map(aChallenger -> client.challenge(theBot, aChallenger::consumeChallengeBuilder))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                if (challengeOpt.isPresent()) {
                    return challengeOpt;
                }
            }

            if (counter++ > 10) {
                log.debug("Braking loop after 10 attempts");
                break;
            }

            if (!sendRequestsAllowed.get() || !sendChallengesAllowed.get()) {
                log.debug("Not sending requests or challenges. Breaking loop.");
                break;
            }
        }

        return Optional.empty();
    }

    @org.springframework.context.event.EventListener(LichessTooManyRequestsSent.class)
    public void onLichessTooManyRequests() {
        log.warn("Lichess API: too many requests. Stop sending requests to lichess.");
        sendRequestsAllowed.set(false);
    }

    @org.springframework.context.event.EventListener(LichessTooManyGamesPlayed.class)
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

    private abstract static class Challenger {
        final StatsPerfType statsPerfType;

        final List<Consumer<ChallengesApiAuthCommon.ChallengeBuilder>> builders = new ArrayList<>();

        int myRating;

        private Challenger(StatsPerfType statsPerfType) {
            this.statsPerfType = statsPerfType;
        }

        void setMyRating(Map<StatsPerfType, StatsPerf> myRatings) {
            myRating = getRating(myRatings);
        }

        boolean filter(User bot) {
            int botRating = getRating(bot.ratings());
            return botRating >= myRating - RATING_THRESHOLD && botRating <= myRating + RATING_THRESHOLD;
        }

        int getRating(Map<StatsPerfType, StatsPerf> ratings) {
            StatsPerf stats = ratings.get(statsPerfType);
            if (stats instanceof StatsPerf.StatsPerfGame statsPerfGame) {
                return statsPerfGame.rating();
            }
            return 0;
        }

        void consumeChallengeBuilder(ChallengesApiAuthCommon.ChallengeBuilder challengeBuilder) {
            Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer = builders.get(rand.nextInt(builders.size()));
            challengeBuilderConsumer.accept(challengeBuilder);
        }
    }


    private static class BulletChallengerBot extends Challenger {
        public BulletChallengerBot() {
            super(StatsPerfType.bullet);

            // Genera demasiado trafico y nos desconecta del server
//            builders.add(challengeBuilder -> challengeBuilder
//                    .clockBullet1m0s()
//                    .color(Enums.ColorPref.random)
//                    .variant(Enums.GameVariant.standard)
//                    .rated(true));

            builders.add(challengeBuilder -> challengeBuilder
                    .clockBullet2m1s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));
        }
    }

    private static class BlitzChallengerBot extends Challenger {
        public BlitzChallengerBot() {
            super(StatsPerfType.blitz);

            builders.add(challengeBuilder -> challengeBuilder
                    .clockBlitz3m0s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));

            builders.add(challengeBuilder -> challengeBuilder
                    .clockBlitz3m2s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));

            builders.add(challengeBuilder -> challengeBuilder
                    .clockBlitz5m3s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));
        }
    }


    private static class RapidChallengerBot extends Challenger {
        public RapidChallengerBot() {
            super(StatsPerfType.rapid);

            builders.add(challengeBuilder -> challengeBuilder
                    .clockRapid10m0s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));

            builders.add(challengeBuilder -> challengeBuilder
                    .clockRapid10m5s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));

            builders.add(challengeBuilder -> challengeBuilder
                    .clockRapid15m10s()
                    .color(Enums.ColorPref.random)
                    .variant(Enums.GameVariant.standard)
                    .rated(true));
        }
    }

}
