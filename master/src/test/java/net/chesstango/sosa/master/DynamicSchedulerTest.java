package net.chesstango.sosa.master;

import net.chesstango.sosa.master.events.LichessTooManyExpired;
import net.chesstango.sosa.master.events.LichessTooManyGamesPlayed;
import net.chesstango.sosa.master.events.LichessTooManyRequestsSent;
import net.chesstango.sosa.master.lichess.RetryIn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class DynamicSchedulerTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private JobDetail challengeBotJobDetails;

    @Mock
    private JobDetail publishExpiredTimerEventRequestsJob;

    @Mock
    private JobDetail publishExpiredTimerEventGamesJob;

    private final JobKey challengeJobKey = new JobKey("challengeBotJob", "group1");
    private final JobKey requestJobKey = new JobKey("publishExpiredRequestsJob", "group1");
    private final JobKey gamesJobKey = new JobKey("publishExpiredGamesJob", "group1");

    @BeforeEach
    void setUp() {
        lenient().when(challengeBotJobDetails.getKey()).thenReturn(challengeJobKey);
        lenient().when(publishExpiredTimerEventRequestsJob.getKey()).thenReturn(requestJobKey);
        lenient().when(publishExpiredTimerEventGamesJob.getKey()).thenReturn(gamesJobKey);
    }

    @Test
    void onLichessTooManyRequestsSent_withChallengeJobEnabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        dynamicScheduler.onLichessTooManyRequestsSent();

        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        Trigger trigger = triggerCaptor.getValue();

        assertThat(trigger).isNotNull();
        assertThat(trigger.getJobKey()).isEqualTo(requestJobKey);
        assertThat(trigger.getKey().getName()).startsWith("PublishTooManyExpiredTimerEventTrigger_Requests-");
        assertThat(trigger.getStartTime()).isNotNull();

        verify(scheduler).pauseJob(challengeJobKey);
    }

    @Test
    void onLichessTooManyRequestsSent_withChallengeJobDisabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                false
        );

        dynamicScheduler.onLichessTooManyRequestsSent();

        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        Trigger trigger = triggerCaptor.getValue();

        assertThat(trigger).isNotNull();
        assertThat(trigger.getJobKey()).isEqualTo(requestJobKey);

        verify(scheduler, never()).pauseJob(any());
    }

    @Test
    void onLichessTooManyRequestsSent_whenScheduleJobFails_throwsRuntimeException() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        when(scheduler.scheduleJob(any(Trigger.class))).thenThrow(new SchedulerException("Scheduler error"));

        RuntimeException exception = assertThrows(RuntimeException.class, dynamicScheduler::onLichessTooManyRequestsSent);
        assertThat(exception.getCause()).isInstanceOf(SchedulerException.class);
        assertThat(exception.getCause()).hasMessage("Scheduler error");
    }

    @Test
    void onLichessTooManyRequestsSent_whenPauseJobFails_throwsRuntimeException() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        doThrow(new SchedulerException("Pause error")).when(scheduler).pauseJob(challengeJobKey);

        RuntimeException exception = assertThrows(RuntimeException.class, dynamicScheduler::onLichessTooManyRequestsSent);
        assertThat(exception.getCause()).isInstanceOf(SchedulerException.class);
        assertThat(exception.getCause()).hasMessage("Pause error");
    }

    @Test
    void onLichessTooManyGamesPlayed_schedulesTrigger() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        RetryIn retryIn = new RetryIn(120);
        LichessTooManyGamesPlayed event = new LichessTooManyGamesPlayed(this, retryIn);

        dynamicScheduler.onLichessTooManyGamesPlayed(event);

        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        Trigger trigger = triggerCaptor.getValue();

        assertThat(trigger).isNotNull();
        assertThat(trigger.getJobKey()).isEqualTo(gamesJobKey);
        assertThat(trigger.getKey().getName()).startsWith("PublishTooManyExpiredTimerEventTrigger_Game-");
        assertThat(trigger.getStartTime()).isNotNull();
    }

    @Test
    void onLichessTooManyGamesPlayed_whenScheduleJobFails_throwsRuntimeException() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        RetryIn retryIn = new RetryIn(60);
        LichessTooManyGamesPlayed event = new LichessTooManyGamesPlayed(this, retryIn);

        when(scheduler.scheduleJob(any(Trigger.class))).thenThrow(new SchedulerException("Schedule games error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> dynamicScheduler.onLichessTooManyGamesPlayed(event));
        assertThat(exception.getCause()).isInstanceOf(SchedulerException.class);
        assertThat(exception.getCause()).hasMessage("Schedule games error");
    }

    @Test
    void onLichessTooManyExpired_withRequests_andChallengeJobEnabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        LichessTooManyExpired event = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.REQUESTS);

        dynamicScheduler.onLichessTooManyExpired(event);

        verify(scheduler).resumeJob(challengeJobKey);
    }

    @Test
    void onLichessTooManyExpired_withRequests_andChallengeJobDisabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                false
        );

        LichessTooManyExpired event = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.REQUESTS);

        dynamicScheduler.onLichessTooManyExpired(event);

        verify(scheduler, never()).resumeJob(any());
    }

    @Test
    void onLichessTooManyExpired_withGames() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        LichessTooManyExpired event = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.GAMES);

        dynamicScheduler.onLichessTooManyExpired(event);

        verifyNoInteractions(scheduler);
    }

    @Test
    void onLichessTooManyExpired_whenResumeJobFails_throwsRuntimeException() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        doThrow(new SchedulerException("Resume error")).when(scheduler).resumeJob(challengeJobKey);

        LichessTooManyExpired event = new LichessTooManyExpired(this, LichessTooManyExpired.ExpirationType.REQUESTS);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> dynamicScheduler.onLichessTooManyExpired(event));
        assertThat(exception.getCause()).isInstanceOf(SchedulerException.class);
        assertThat(exception.getCause()).hasMessage("Resume error");
    }

    @Test
    void resumeChallengeBotJob_withChallengeJobEnabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                true
        );

        dynamicScheduler.resumeChallengeBotJob();

        verify(scheduler).resumeJob(challengeJobKey);
    }

    @Test
    void resumeChallengeBotJob_withChallengeJobDisabled() throws SchedulerException {
        DynamicScheduler dynamicScheduler = new DynamicScheduler(
                scheduler,
                challengeBotJobDetails,
                publishExpiredTimerEventRequestsJob,
                publishExpiredTimerEventGamesJob,
                2,
                false
        );

        dynamicScheduler.resumeChallengeBotJob();

        verify(scheduler, never()).resumeJob(any());
    }
}
