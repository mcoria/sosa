package net.chesstango.sosa.master.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class QuerySchedulerTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private Trigger challengeBotJobTrigger;

    @Test
    @DisplayName("getStatus returns disabled status with null dates when trigger is null")
    void getStatus_whenTriggerIsNull() {
        QueryScheduler queryScheduler = new QueryScheduler(scheduler, null);

        ScheduleStatus status = queryScheduler.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.challengeJobEnabled()).isFalse();
        assertThat(status.challengeJobLastExecution()).isNull();
        assertThat(status.challengeJobNextExecution()).isNull();

        verifyNoInteractions(scheduler);
    }

    @ParameterizedTest
    @EnumSource(value = Trigger.TriggerState.class, names = {"NORMAL", "BLOCKED"})
    @DisplayName("getStatus returns enabled status and execution dates when trigger is active")
    void getStatus_whenTriggerIsActive(Trigger.TriggerState activeState) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey("challengeBotJobTrigger", "group1");
        Date previousFire = new Date(1_000_000L);
        Date nextFire = new Date(2_000_000L);

        when(challengeBotJobTrigger.getKey()).thenReturn(triggerKey);
        when(scheduler.getTriggerState(triggerKey)).thenReturn(activeState);
        when(challengeBotJobTrigger.getPreviousFireTime()).thenReturn(previousFire);
        when(challengeBotJobTrigger.getNextFireTime()).thenReturn(nextFire);

        QueryScheduler queryScheduler = new QueryScheduler(scheduler, challengeBotJobTrigger);

        ScheduleStatus status = queryScheduler.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.challengeJobEnabled()).isTrue();
        assertThat(status.challengeJobLastExecution()).isEqualTo(previousFire);
        assertThat(status.challengeJobNextExecution()).isEqualTo(nextFire);

        verify(scheduler).getTriggerState(triggerKey);
    }

    @ParameterizedTest
    @EnumSource(value = Trigger.TriggerState.class, names = {"PAUSED", "COMPLETE", "ERROR", "NONE"})
    @DisplayName("getStatus returns disabled status with execution dates when trigger is inactive")
    void getStatus_whenTriggerIsInactive(Trigger.TriggerState inactiveState) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey("challengeBotJobTrigger", "group1");
        Date previousFire = new Date(1_000_000L);
        Date nextFire = new Date(2_000_000L);

        when(challengeBotJobTrigger.getKey()).thenReturn(triggerKey);
        when(scheduler.getTriggerState(triggerKey)).thenReturn(inactiveState);
        when(challengeBotJobTrigger.getPreviousFireTime()).thenReturn(previousFire);
        when(challengeBotJobTrigger.getNextFireTime()).thenReturn(nextFire);

        QueryScheduler queryScheduler = new QueryScheduler(scheduler, challengeBotJobTrigger);

        ScheduleStatus status = queryScheduler.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.challengeJobEnabled()).isFalse();
        assertThat(status.challengeJobLastExecution()).isEqualTo(previousFire);
        assertThat(status.challengeJobNextExecution()).isEqualTo(nextFire);

        verify(scheduler).getTriggerState(triggerKey);
    }

    @Test
    @DisplayName("getStatus handles SchedulerException gracefully returning disabled status with null dates")
    void getStatus_whenSchedulerThrowsException() throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey("challengeBotJobTrigger", "group1");

        when(challengeBotJobTrigger.getKey()).thenReturn(triggerKey);
        when(scheduler.getTriggerState(triggerKey)).thenThrow(new SchedulerException("Scheduler error"));

        QueryScheduler queryScheduler = new QueryScheduler(scheduler, challengeBotJobTrigger);

        ScheduleStatus status = queryScheduler.getStatus();

        assertThat(status).isNotNull();
        assertThat(status.challengeJobEnabled()).isFalse();
        assertThat(status.challengeJobLastExecution()).isNull();
        assertThat(status.challengeJobNextExecution()).isNull();

        verify(scheduler).getTriggerState(triggerKey);
        verify(challengeBotJobTrigger, never()).getPreviousFireTime();
        verify(challengeBotJobTrigger, never()).getNextFireTime();
    }
}
