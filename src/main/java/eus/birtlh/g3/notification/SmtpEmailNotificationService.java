package eus.birtlh.g3.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Adaptador SMTP que envía notificaciones de correo mediante {@link JavaMailSender}.
 *
 * <p>El remitente se obtiene de la cuenta configurada en {@code spring.mail.username}, mientras
 * que la conexión con el proveedor se define externamente mediante propiedades de Spring Mail.</p>
 */
@Service
@RequiredArgsConstructor
public class SmtpEmailNotificationService implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * Construye y envía un correo de texto simple mediante la conexión SMTP configurada.
     *
     * @param emailNotification mensaje con destinatario, asunto y contenido
     */
    @Override
    public void sendEmail(EmailNotification emailNotification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(emailNotification.recipient());
        message.setSubject(emailNotification.subject());
        message.setText(emailNotification.body());

        mailSender.send(message);
    }
}
