package eus.birtlh.g3.notification;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.event.PreventiveAssignedEvent;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveAssignedTelegramNotificationListenerTest {

    @Mock
    private PreventiveRepository preventiveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramNotificationService telegramNotificationService;

    @Mock
    private Preventive preventive;

    @Mock
    private User inspector;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @InjectMocks
    private PreventiveAssignedTelegramNotificationListener listener;

    /** Comprueba que una asignación confirmada construye el mensaje de Telegram correcto. */
    @DisplayName("La asignación confirmada envía por Telegram el detalle del preventivo")
    @Test
    void enviaNotificacionConElPlanLaFechaYElInspector() {
        prepareAssignedPreventive();

        listener.handle(new PreventiveAssignedEvent(10L, 20L));

        verify(telegramNotificationService).sendMessage(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).as("El mensaje incluye el plan, la fecha y el inspector")
                .contains("Plan de prueba", "2030-01-10", "inspector@test.local");
    }

    /** Comprueba que un error de Telegram queda contenido en el listener y no se propaga al flujo. */
    @DisplayName("Un fallo de Telegram no se propaga tras asignar el preventivo")
    @Test
    void noPropagaElFalloDeTelegram() {
        prepareAssignedPreventive();
        doThrow(new RestClientException("Fallo de Telegram de prueba"))
                .when(telegramNotificationService).sendMessage(anyString());

        assertThatCode(() -> listener.handle(new PreventiveAssignedEvent(10L, 20L)))
                .as("El listener contiene el error del proveedor de Telegram")
                .doesNotThrowAnyException();
        verify(telegramNotificationService).sendMessage(anyString());
    }

    private void prepareAssignedPreventive() {
        when(preventiveRepository.findById(10L)).thenReturn(Optional.of(preventive));
        when(userRepository.findById(20L)).thenReturn(Optional.of(inspector));
        when(inspector.getEmail()).thenReturn("inspector@test.local");
        when(preventive.getPlanName()).thenReturn("Plan de prueba");
        when(preventive.getDueDate()).thenReturn(LocalDate.of(2030, 1, 10));
    }
}
