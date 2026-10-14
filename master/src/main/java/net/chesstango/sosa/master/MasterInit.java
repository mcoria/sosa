package net.chesstango.sosa.master;

import chariot.model.UserAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.LichessChallengerBot;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.master.lichess.BotEventsLoopReader;
import org.springframework.boot.context.event.ApplicationReadyEvent;
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
    private final BotEventsLoopReader botEventsLoopReader;
    private final ThreadPoolTaskExecutor taskExecutor;

    public MasterInit(SosaState sosaState,
                      LichessClient lichessClient,
                      LichessChallengerBot lichessChallengerBot,
                      BotEventsLoopReader botEventsLoopReader,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.sosaState = sosaState;
        this.lichessClient = lichessClient;
        this.lichessChallengerBot = lichessChallengerBot;
        this.botEventsLoopReader = botEventsLoopReader;
        this.taskExecutor = taskExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("ApplicationReadyEvent received");

        UserAuth myProfile = lichessClient.getProfile();

        sosaState.setMyProfile(myProfile);

        lichessChallengerBot.updateRatings();

        taskExecutor.submit(botEventsLoopReader);
    }
}
