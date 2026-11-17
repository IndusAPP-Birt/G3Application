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
import org.springframework.mail.MailSendException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveAssignedEmailNotificationListenerTest {

    @Mock
    private PreventiveRepository preventiveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private Preventive preventive;

    @Mock
    private User inspector;

    @Captor
    private ArgumentCaptor<EmailNotification> notificationCaptor;

    @InjectMocks
    private PreventiveAssignedEmailNotificationListener listener;

    /** Comprueba que una asignación confirmada construye el correo del inspector correcto. */
    @DisplayName("La asignación confirmada envía al inspector el detalle del preventivo")
    @Test
    void enviaNotificacionConElPlanYLaFechaPrevista() {
        prepareAssignedPreventive();

        listener.handle(new PreventiveAssignedEvent(10L, 20L));

        verify(emailNotificationService).sendEmail(notificationCaptor.capture());
        EmailNotification notification = notificationCaptor.getValue();
        assertThat(notification.recipient()).as("El destinatario es el inspector asignado")
                .isEqualTo("inspector@test.local");
        assertThat(notification.subject()).as("El asunto identifica la nueva asignación")
                .isEqualTo("Nueva asignación de mantenimiento preventivo");
        assertThat(notification.body()).as("El correo incluye el plan y la fecha prevista")
                .contains("Plan de prueba", "2030-01-10");
    }

    /** Comprueba que un error SMTP queda contenido en el listener y no se propaga al flujo. */
    @DisplayName("Un fallo SMTP no se propaga tras asignar el preventivo")
    @Test
    void noPropagaElFalloDelProveedorSMTP() {
        prepareAssignedPreventive();
        doThrow(new MailSendException("Fallo SMTP de prueba"))
                .when(emailNotificationService).sendEmail(any(EmailNotification.class));

        assertThatCode(() -> listener.handle(new PreventiveAssignedEvent(10L, 20L)))
                .as("El listener contiene el error del proveedor SMTP")
                .doesNotThrowAnyException();
        verify(emailNotificationService).sendEmail(any(EmailNotification.class));
    }

    private void prepareAssignedPreventive() {
        when(preventiveRepository.findById(10L)).thenReturn(Optional.of(preventive));
        when(userRepository.findById(20L)).thenReturn(Optional.of(inspector));
        when(inspector.getEmail()).thenReturn("inspector@test.local");
        when(preventive.getPlanName()).thenReturn("Plan de prueba");
        when(preventive.getDueDate()).thenReturn(LocalDate.of(2030, 1, 10));
    }
}
