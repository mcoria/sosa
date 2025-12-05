package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Service
public class LichessChallengerBot {
    public static final int RATING_THRESHOLD = 150;

    private static final Random rand = new Random();

    private final LichessClient client;

    private final BotQueue botQueue;

    private final List<Challenger> challengerTypes;

    public LichessChallengerBot(LichessClient client,
                                BotQueue botQueue,
                                @Value("${app.challengeTypes}") List<String> challengeTypes) {
        this.client = client;
        this.botQueue = botQueue;
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

    @Async
    public void updateRating() {
        log.info("Getting my ratings");

        Map<StatsPerfType, StatsPerf> myRatings = client.getRatings();

        challengerTypes.forEach(challenger -> challenger.setRating(myRatings));
    }

    public Optional<Challenge> challengeRandomBot() {
        Challenge challenge = null;

        User aBot = botQueue.pickBot();

        if (aBot != null) {
            List<Challenger> challengerTypesShuffled = new ArrayList<>(challengerTypes);

            Collections.shuffle(challengerTypesShuffled);

            Optional<Challenger> challengerOpt = challengerTypesShuffled
                    .stream()
                    .filter(aChallenger -> aChallenger.filer(aBot))
                    .findFirst();

            if (challengerOpt.isPresent()) {
                Challenger challenger = challengerOpt.get();
                challenge = client.challenge(aBot, challenger::consumeChallengeBuilder);
            } else {
                log.info("Rating: bot {} filtered out ", aBot.id());
            }
        } else {
            log.warn("No bots online :S");
        }
        return challenge == null ? Optional.empty() : Optional.of(challenge);
    }

    private abstract static class Challenger {
        final StatsPerfType statsPerfType;

        final List<Consumer<ChallengesApiAuthCommon.ChallengeBuilder>> builders = new ArrayList<>();

        int myRating;

        private Challenger(StatsPerfType statsPerfType) {
            this.statsPerfType = statsPerfType;
        }

        void setRating(Map<StatsPerfType, StatsPerf> myRatings) {
            myRating = getRating(myRatings);
        }

        boolean filer(User bot) {
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
            Consumer<ChallengesApiAuthCommon.ChallengeBuilder> element = builders.get(rand.nextInt(builders.size()));
            element.accept(challengeBuilder);
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
