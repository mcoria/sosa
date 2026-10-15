package net.chesstango.sosa.master;

import chariot.model.UserAuth;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.sosa.master.lichess.BotEventsLoopReader;
import net.chesstango.sosa.master.lichess.ChallengerBot;
import net.chesstango.sosa.master.lichess.LichessClient;
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
    private final ChallengerBot challengerBot;
    private final BotEventsLoopReader botEventsLoopReader;
    private final ThreadPoolTaskExecutor taskExecutor;

    public MasterInit(SosaState sosaState,
                      LichessClient lichessClient,
                      ChallengerBot challengerBot,
                      BotEventsLoopReader botEventsLoopReader,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.sosaState = sosaState;
        this.lichessClient = lichessClient;
        this.challengerBot = challengerBot;
        this.botEventsLoopReader = botEventsLoopReader;
        this.taskExecutor = taskExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        log.info("ApplicationReadyEvent received");

        UserAuth myProfile = lichessClient.getProfile();

        sosaState.setMyProfile(myProfile);

        challengerBot.setMyProfile(myProfile);

        taskExecutor.submit(botEventsLoopReader);
    }
}
