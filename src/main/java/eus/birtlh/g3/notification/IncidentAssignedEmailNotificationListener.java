package eus.birtlh.g3.notification;

import eus.birtlh.g3.incident.Incident;
import eus.birtlh.g3.incident.event.IncidentAssignedEvent;
import eus.birtlh.g3.incident.repository.IncidentRepository;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía una notificación de correo cuando se asigna una incidencia a un técnico de mantenimiento.
 *
 * <p>Escucha el evento después de confirmar la asignación y traduce sus datos a un
 * {@link EmailNotification}. Los fallos de envío se registran sin afectar a la incidencia ya
 * persistida.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IncidentAssignedEmailNotificationListener {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * Procesa una asignación confirmada y notifica al técnico asociado.
     *
     * <p>Resuelve de nuevo la incidencia y el técnico mediante sus identificadores para construir
     * el mensaje sin acoplar el evento a entidades JPA.</p>
     *
     * @param event evento publicado al asignar un técnico a una incidencia
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(IncidentAssignedEvent event) {
        Incident incident = incidentRepository.findById(event.incidentId())
                .orElse(null);
        User maintenanceTechnician = userRepository.findById(event.maintenanceTechnicianId())
                .orElse(null);

        if (incident == null || maintenanceTechnician == null) {
            log.warn(
                    "No se puede enviar la notificación de asignación de la incidencia {}.",
                    event.incidentId()
            );
            return;
        }

        EmailNotification notification = new EmailNotification(
                maintenanceTechnician.getEmail(),
                "Nueva incidencia de mantenimiento asignada",
                """
                Se te ha asignado una incidencia de mantenimiento.

                Incidencia: %s
                Descripción: %s
                Gravedad: %s.
                """.formatted(
                        incident.getId(),
                        incident.getDescription(),
                        incident.getSeverity()
                )
        );

        try {
            emailNotificationService.sendEmail(notification);
        } catch (MailException exception) {
            log.error(
                    "No se ha podido enviar la notificación de la incidencia {} al técnico {}.",
                    event.incidentId(),
                    event.maintenanceTechnicianId(),
                    exception
            );
        }
    }
}
