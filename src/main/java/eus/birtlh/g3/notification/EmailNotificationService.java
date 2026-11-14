package eus.birtlh.g3.notification;

/**
 * Puerto de salida para enviar notificaciones por correo electrónico.
 *
 * <p>Los módulos de negocio dependen de este contrato, no de un proveedor concreto como Gmail.</p>
 */
public interface EmailNotificationService {

    /**
     * Envía un mensaje de correo electrónico.
     *
     * @param emailNotification mensaje que se desea enviar
     */
    void sendEmail(EmailNotification emailNotification);
}
