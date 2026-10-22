package net.chesstango.sosa.master.queues;

import net.chesstango.sosa.master.SosaState;
import net.chesstango.sosa.master.lichess.LichessClient;
import net.chesstango.sosa.messages.master.SendAbort;
import net.chesstango.sosa.messages.master.SendMove;
import net.chesstango.sosa.messages.master.WorkerBusy;
import net.chesstango.sosa.messages.master.WorkerReady;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class MasterConsumerTest {

    @Mock
    private LichessClient client;

    @Mock
    private SosaState sosaState;

    private MasterConsumer masterConsumer;

    @BeforeEach
    void setUp() {
        masterConsumer = new MasterConsumer(client, sosaState);
    }

    @Test
    void handleWorkerReadyTest() {
        WorkerReady workerReady = new WorkerReady("worker-1");

        masterConsumer.handle(workerReady);

        verify(sosaState).addAvailableWorker("worker-1");
        verifyNoInteractions(client);
    }

    @Test
    void handleWorkerBusyTest() {
        WorkerBusy workerBusy = new WorkerBusy("worker-1");

        masterConsumer.handle(workerBusy);

        verify(sosaState).removeAvailableWorker("worker-1");
        verifyNoInteractions(client);
    }

    @Test
    void handleSendMoveTest() {
        SendMove sendMove = new SendMove("game123", "e2e4");

        masterConsumer.handle(sendMove);

        verify(client).gameMove("game123", "e2e4");
        verifyNoInteractions(sosaState);
    }

    @Test
    void handleSendAbortTest() {
        SendAbort sendAbort = new SendAbort("game123");

        masterConsumer.handle(sendAbort);

        verify(client).gameAbort("game123");
        verifyNoInteractions(sosaState);
    }
}
