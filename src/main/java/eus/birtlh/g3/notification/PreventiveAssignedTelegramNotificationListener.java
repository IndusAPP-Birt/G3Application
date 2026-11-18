package eus.birtlh.g3.notification;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.event.PreventiveAssignedEvent;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

/**
 * Envía una notificación central de Telegram cuando se asigna un inspector a un preventivo.
 *
 * <p>Escucha el evento después de confirmar la asignación y obtiene de nuevo los datos del
 * preventivo y del inspector para construir el mensaje. Los fallos del proveedor se registran
 * sin afectar a la ejecución preventiva ya persistida.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PreventiveAssignedTelegramNotificationListener {

    private final PreventiveRepository preventiveRepository;
    private final UserRepository userRepository;
    private final TelegramNotificationService telegramNotificationService;

    /**
     * Procesa una asignación confirmada y la comunica al chat central de Telegram.
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
                    "No se puede enviar la notificación de Telegram del preventivo {}.",
                    event.preventiveId()
            );
            return;
        }

        String message = """
                Nueva asignación de mantenimiento preventivo

                Preventivo: %s
                Fecha prevista: %s
                Inspector asignado: %s
                """.formatted(
                preventive.getPlanName(),
                preventive.getDueDate(),
                inspector.getEmail()
        );

        try {
            telegramNotificationService.sendMessage(message);
        } catch (RestClientException exception) {
            log.error(
                    "No se ha podido enviar por Telegram la asignación del preventivo {}.",
                    event.preventiveId(),
                    exception
            );
        }
    }
}
