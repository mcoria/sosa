package net.chesstango.sosa.master;

import chariot.model.UserAuth;
import chariot.model.UserProfileData;
import net.chesstango.sosa.master.events.LichessMainLoopFinished;
import net.chesstango.sosa.master.lichess.BotEventsLoopReader;
import net.chesstango.sosa.master.lichess.LichessClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class MasterInitTest {

    @Mock
    private SosaState sosaState;

    @Mock
    private LichessClient lichessClient;

    @Mock
    private BotEventsLoopReader botEventsLoopReader;

    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private MasterInit masterInit;

    @BeforeEach
    void setUp() {
        masterInit = new MasterInit(sosaState, lichessClient, botEventsLoopReader, applicationEventPublisher, taskExecutor);
    }

    @Test
    void onApplicationReadyEventTest() {
        masterInit.onApplicationReadyEvent();

        verify(taskExecutor).submit(masterInit);
    }

    @Test
    void onLichessMainLoopFinishedTest() {
        masterInit.onLichessMainLoopFinished();

        verify(taskExecutor).submit(masterInit);
    }

    @Test
    void runTest() {
        UserAuth myProfile = mock(UserProfileData.class);
        when(lichessClient.getProfile()).thenReturn(myProfile);

        masterInit.run();

        verify(lichessClient).getProfile();
        verify(sosaState).setMyProfile(myProfile);
        verify(botEventsLoopReader).run();
        verify(applicationEventPublisher).publishEvent(any(LichessMainLoopFinished.class));
    }
}
