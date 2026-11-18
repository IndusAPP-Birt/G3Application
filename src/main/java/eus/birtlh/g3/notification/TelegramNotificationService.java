package eus.birtlh.g3.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de Telegram que envía notificaciones de texto al chat central configurado.
 *
 * <p>El token del bot y el identificador de chat se obtienen de las propiedades
 * {@code telegram.bot-token} y {@code telegram.chat-id}, resueltas desde las variables de
 * entorno de la aplicación.</p>
 */
@Service
public class TelegramNotificationService {

    private final RestClient restClient;
    private final String botToken;
    private final String chatId;

    public TelegramNotificationService(
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId
    ) {
        this.restClient = RestClient.create("https://api.telegram.org");
        this.botToken = botToken;
        this.chatId = chatId;
    }

    /**
     * Envía un mensaje de texto al chat central mediante la API de Telegram.
     *
     * @param text contenido de la notificación que se enviará
     */
    public void sendMessage(String text) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("chat_id", chatId);
        form.add("text", text);

        restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
