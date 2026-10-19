package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class ChallengerBot {
    public static final int RATING_THRESHOLD = 300;
    public static final int COUNTER_CHALLENGER = 20;

    private static final Random rand = new Random();

    private final LichessClient client;

    private final BotQueue botQueue;

    private final List<Challenger> challengerTypes;


    public ChallengerBot(LichessClient client,
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

    public Optional<Challenge> challengeRandomBot(UserAuth myProfile) {
        log.info("Challenging random bot");

        List<Challenger> challengerTypesRandom = new ArrayList<>(this.challengerTypes);

        Collections.shuffle(challengerTypesRandom, rand);

        int counter = 0;
        for (User bot = botQueue.pickBot(); bot != null && counter < COUNTER_CHALLENGER; bot = botQueue.pickBot(), counter++) {
            final User theBot = bot;
            // Avoid Fail[status=400, info=Info[message={"error":"You cannot challenge yourself"}]]
            if (!Objects.equals(bot.id(), myProfile.id())) {
                if (client.isOnline(theBot)) {
                    return challengerTypesRandom
                            .stream()
                            .filter(aChallenger -> aChallenger.filter(myProfile, theBot))
                            .peek(aChallenger -> log.info("Challenging bot {} {}", theBot.id(), aChallenger.getClass().getSimpleName()))
                            .map(aChallenger -> client.challenge(theBot, aChallenger::consumeChallengeBuilder))
                            .filter(Optional::isPresent)
                            .peek(challenge -> log.info("Challenging bot {} succeeded", theBot.id()))
                            .map(Optional::get)
                            .findFirst();

                } else {
                    log.info("Bot {} is offline", theBot.id());
                }
            }
        }

        return Optional.empty();
    }

    abstract static class Challenger {
        final StatsPerfType statsPerfType;

        final List<Consumer<ChallengesApiAuthCommon.ChallengeBuilder>> builders = new ArrayList<>();

        private Challenger(StatsPerfType statsPerfType) {
            this.statsPerfType = statsPerfType;
        }


        boolean filter(UserAuth myProfile, User bot) {
            int myRating = getRating(myProfile.ratings());
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


    class BulletChallengerBot extends Challenger {
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

    class BlitzChallengerBot extends Challenger {
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


    class RapidChallengerBot extends Challenger {
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
