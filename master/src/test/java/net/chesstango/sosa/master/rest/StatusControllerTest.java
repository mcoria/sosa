package net.chesstango.sosa.master.rest;

import net.chesstango.sosa.master.Challenge;
import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.SosaStatus;
import net.chesstango.sosa.master.Worker;
import net.chesstango.sosa.master.scheduler.QueryScheduler;
import net.chesstango.sosa.master.scheduler.ScheduleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class StatusControllerTest {

    @Mock
    private SosaState sosaState;

    @Mock
    private QueryScheduler queryScheduler;

    @InjectMocks
    private StatusController statusController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statusController).build();
    }

    @Test
    @DisplayName("get returns map combining SosaStatus and ScheduleStatus")
    void get_returnsCombinedStatusMap() {
        List<Worker> workers = List.of(new Worker("worker-1"), new Worker("worker-2"));
        List<Challenge> challenges = List.of(new Challenge("challenge-1"));
        List<String> games = List.of("game-1", "game-2");
        SosaStatus sosaStatus = new SosaStatus(workers, challenges, games);

        Date lastExecution = new Date(1_700_000_000_000L);
        Date nextExecution = new Date(1_700_000_060_000L);
        ScheduleStatus scheduleStatus = new ScheduleStatus(true, lastExecution, nextExecution);

        when(sosaState.getStatus()).thenReturn(sosaStatus);
        when(queryScheduler.getStatus()).thenReturn(scheduleStatus);

        Map<String, Object> result = statusController.get();

        assertThat(result).isNotNull();
        assertThat(result).containsEntry("availableWorkers", workers);
        assertThat(result).containsEntry("inProgressChallenges", challenges);
        assertThat(result).containsEntry("inProgressGames", games);
        assertThat(result).containsEntry("challengeJobEnabled", true);
        assertThat(result).containsEntry("challengeJobLastExecution", lastExecution);
        assertThat(result).containsEntry("challengeJobNextExecution", nextExecution);

        verify(sosaState).getStatus();
        verify(queryScheduler).getStatus();
    }

    @Test
    @DisplayName("get returns empty collections and null dates when state is empty")
    void get_whenEmptyState() {
        SosaStatus sosaStatus = new SosaStatus(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ScheduleStatus scheduleStatus = new ScheduleStatus(false, null, null);

        when(sosaState.getStatus()).thenReturn(sosaStatus);
        when(queryScheduler.getStatus()).thenReturn(scheduleStatus);

        Map<String, Object> result = statusController.get();

        assertThat(result).isNotNull();
        assertThat((Iterable<?>) result.get("availableWorkers")).isEmpty();
        assertThat((Iterable<?>) result.get("inProgressChallenges")).isEmpty();
        assertThat((Iterable<?>) result.get("inProgressGames")).isEmpty();
        assertThat(result.get("challengeJobEnabled")).isEqualTo(false);
        assertThat(result.get("challengeJobLastExecution")).isNull();
        assertThat(result.get("challengeJobNextExecution")).isNull();

        verify(sosaState).getStatus();
        verify(queryScheduler).getStatus();
    }

    @Test
    @DisplayName("GET /status endpoint returns HTTP 200 with JSON payload")
    void get_viaMockMvc_returnsOkAndJson() throws Exception {
        Worker worker = new Worker("worker-1");
        Challenge challenge = new Challenge("challenge-1");
        SosaStatus sosaStatus = new SosaStatus(List.of(worker), List.of(challenge), List.of("game-1"));
        ScheduleStatus scheduleStatus = new ScheduleStatus(true, null, null);

        when(sosaState.getStatus()).thenReturn(sosaStatus);
        when(queryScheduler.getStatus()).thenReturn(scheduleStatus);

        mockMvc.perform(get("/status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableWorkers[0].workerId").value("worker-1"))
                .andExpect(jsonPath("$.inProgressChallenges[0].challengeId").value("challenge-1"))
                .andExpect(jsonPath("$.inProgressGames[0]").value("game-1"))
                .andExpect(jsonPath("$.challengeJobEnabled").value(true))
                .andExpect(jsonPath("$.challengeJobLastExecution").doesNotExist())
                .andExpect(jsonPath("$.challengeJobNextExecution").doesNotExist());

        verify(sosaState).getStatus();
        verify(queryScheduler).getStatus();
    }
}
