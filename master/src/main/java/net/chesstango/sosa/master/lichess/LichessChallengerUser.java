package net.chesstango.sosa.master.lichess;

import chariot.api.ChallengesApiAuthCommon;
import chariot.model.Challenge;
import chariot.model.Enums;
import chariot.model.UserAuth;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class LichessChallengerUser {
    private final LichessClient client;

    public LichessChallengerUser(LichessClient client) {
        this.client = client;
    }

    public Optional<Challenge> challengeUser(String username, ChallengeType challengeType) {
        Consumer<ChallengesApiAuthCommon.ChallengeBuilder> challengeBuilderConsumer = (builder) -> {
            switch (challengeType) {
                case BULLET -> builder.clockBullet2m1s()
                        .color(Enums.ColorPref.random)
                        .variant(Enums.GameVariant.standard)
                        .rated(true);
                case BLITZ -> builder.clockBlitz5m3s()
                        .color(Enums.ColorPref.random)
                        .variant(Enums.GameVariant.standard)
                        .rated(true);
                case RAPID -> builder.clockRapid10m0s()
                        .color(Enums.ColorPref.random)
                        .variant(Enums.GameVariant.standard)
                        .rated(true);
            }
        };

        Optional<UserAuth> user = client.findUser(username);

        if (user.isPresent()) {
            return Optional.of(client.challenge(user.get(), challengeBuilderConsumer));
        }

        log.info("User '{}' not found", username);
        return Optional.empty();
    }
}
