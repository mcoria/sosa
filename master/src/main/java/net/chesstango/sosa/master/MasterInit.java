package net.chesstango.sosa.master;

import chariot.model.UserAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.events.LichessConnected;
import net.chesstango.sosa.master.lichess.LichessChallengerBot;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.LichessMainEventsReader;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Mauricio Coria
 */
@Slf4j
@Component
public class MasterInit {
    private final SosaState sosaState;
    private final LichessClient lichessClient;
    private final LichessChallengerBot lichessChallengerBot;
    private final LichessMainEventsReader lichessMainEventsReader;
    private final ThreadPoolTaskExecutor taskExecutor;

    public MasterInit(SosaState sosaState, LichessClient lichessClient,
                      LichessChallengerBot lichessChallengerBot,
                      LichessMainEventsReader lichessMainEventsReader,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.sosaState = sosaState;
        this.lichessClient = lichessClient;
        this.lichessChallengerBot = lichessChallengerBot;
        this.lichessMainEventsReader = lichessMainEventsReader;
        this.taskExecutor = taskExecutor;
    }

    @EventListener(LichessConnected.class)
    public void onLichessConnected() {
        log.info("LichessConnected event received");

        UserAuth myProfile = lichessClient.getProfile();

        sosaState.setMyProfile(myProfile);

        lichessChallengerBot.updateRating(myProfile);

        taskExecutor.submit(lichessMainEventsReader);
    }
}
