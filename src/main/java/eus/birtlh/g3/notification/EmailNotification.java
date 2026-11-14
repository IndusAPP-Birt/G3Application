package eus.birtlh.g3.notification;

/**
 * Mensaje de correo electrónico listo para su envío mediante un canal de notificación.
 *
 * <p>No contiene datos de un dominio concreto para poder reutilizarse en preventivos,
 * incidencias u otros casos de uso.</p>
 *
 * @param recipient dirección de correo electrónico destinataria
 * @param subject asunto del mensaje
 * @param body contenido de texto del mensaje
 */
public record EmailNotification(
        String recipient,
        String subject,
        String body
) {
}
