package net.chesstango.sosa.master.lichess;

import chariot.ClientAuth;
import chariot.api.AccountApiAuth;
import chariot.model.Err;
import chariot.model.One;
import net.chesstango.sosa.master.events.LichessApiCallFailed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class LichessClientImpTest {

    @Mock
    ClientAuth client;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    LichessClientImp lichessClientImp;

    @BeforeEach
    void before() {
        lichessClientImp = new LichessClientImp(client, applicationEventPublisher);
    }

    @Test
    void apiCallFailedTest() {
        AccountApiAuth accountApiAuth = mock(AccountApiAuth.class);

        when(client.account()).thenReturn(accountApiAuth);
        when(accountApiAuth.profile()).thenReturn(One.fail(1, Err.from("Error message")));

        assertThrows(RuntimeException.class, () -> lichessClientImp.getProfile());

        verify(applicationEventPublisher, times(1)).publishEvent(any(LichessApiCallFailed.class));
    }
}
