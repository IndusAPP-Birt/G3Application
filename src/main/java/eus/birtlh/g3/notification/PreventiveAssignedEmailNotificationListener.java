package eus.birtlh.g3.notification;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.event.PreventiveAssignedEvent;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía una notificación de correo cuando un inspector recibe un preventivo.
 *
 * <p>Escucha el evento después de confirmar la asignación y traduce sus datos a un
 * {@link EmailNotification}. Los fallos de envío se registran sin afectar a la ejecución
 * preventiva ya persistida.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PreventiveAssignedEmailNotificationListener {

    private final PreventiveRepository preventiveRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Procesa una asignación confirmada y notifica al inspector asociado.
     *
     * <p>Resuelve de nuevo el preventivo y el inspector mediante sus identificadores para
     * construir el mensaje sin acoplar el evento a entidades JPA.</p>
     *
     * @param event evento publicado al asignar un inspector a un preventivo
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PreventiveAssignedEvent event) {
        Preventive preventive = preventiveRepository.findById(event.preventiveId())
                .orElse(null);
        User inspector = userRepository.findById(event.inspectorId())
                .orElse(null);

        if (preventive == null || inspector == null) {
            log.warn(
                    "No se puede enviar la notificación de asignación del preventivo {}.",
                    event.preventiveId()
            );
            return;
        }

        EmailNotification notification = new EmailNotification(
                inspector.getEmail(),
                "Nueva asignación de mantenimiento preventivo",
                """
                Se te ha asignado el preventivo "%s".
                Fecha prevista: %s.
                """.formatted(preventive.getPlanName(), preventive.getDueDate())
        );

        try {
            emailNotificationService.sendEmail(notification);
        } catch (MailException exception) {
            log.error(
                    "No se ha podido enviar la notificación del preventivo {} al inspector {}.",
                    event.preventiveId(),
                    event.inspectorId(),
                    exception
            );
        }
    }
}
