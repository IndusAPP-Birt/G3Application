package eus.birtlh.g3.notification;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.event.PreventiveAssignedEvent;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveAssignedEmailNotificationListenerTest {

    private static final long PREVENTIVE_ID = 12L;
    private static final long INSPECTOR_ID = 7L;

    @Mock
    private PreventiveRepository preventiveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Captor
    private ArgumentCaptor<EmailNotification> notificationCaptor;

    @InjectMocks
    private PreventiveAssignedEmailNotificationListener listener;

    @Test
    void sendsEmailToInspectorWhenPreventiveAndInspectorExist() {
        Preventive preventive = mock(Preventive.class);
        User inspector = mock(User.class);
        when(preventiveRepository.findById(PREVENTIVE_ID)).thenReturn(Optional.of(preventive));
        when(userRepository.findById(INSPECTOR_ID)).thenReturn(Optional.of(inspector));
        when(preventive.getPlanName()).thenReturn("Revisión trimestral");
        when(preventive.getDueDate()).thenReturn(LocalDate.of(2026, 12, 1));
        when(inspector.getEmail()).thenReturn("inspector@g3.test");

        listener.handle(new PreventiveAssignedEvent(PREVENTIVE_ID, INSPECTOR_ID));

        verify(emailNotificationService).sendEmail(notificationCaptor.capture());
        EmailNotification notification = notificationCaptor.getValue();
        assertThat(notification.recipient()).isEqualTo("inspector@g3.test");
        assertThat(notification.subject()).isEqualTo("Nueva asignación de mantenimiento preventivo");
        assertThat(notification.body())
                .contains("Revisión trimestral")
                .contains("2026-12-01");
    }

    @Test
    void doesNotSendEmailWhenPreventiveDoesNotExist() {
        when(preventiveRepository.findById(PREVENTIVE_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(INSPECTOR_ID)).thenReturn(Optional.of(mock(User.class)));

        listener.handle(new PreventiveAssignedEvent(PREVENTIVE_ID, INSPECTOR_ID));

        verifyNoInteractions(emailNotificationService);
    }

    @Test
    void doesNotPropagateMailException() {
        Preventive preventive = mock(Preventive.class);
        User inspector = mock(User.class);
        when(preventiveRepository.findById(PREVENTIVE_ID)).thenReturn(Optional.of(preventive));
        when(userRepository.findById(INSPECTOR_ID)).thenReturn(Optional.of(inspector));
        when(preventive.getPlanName()).thenReturn("Revisión trimestral");
        when(preventive.getDueDate()).thenReturn(LocalDate.of(2026, 12, 1));
        when(inspector.getEmail()).thenReturn("inspector@g3.test");
        doThrow(new MailSendException("SMTP no disponible"))
                .when(emailNotificationService)
                .sendEmail(any(EmailNotification.class));

        assertThatCode(() -> listener.handle(new PreventiveAssignedEvent(PREVENTIVE_ID, INSPECTOR_ID)))
                .doesNotThrowAnyException();
    }
}
