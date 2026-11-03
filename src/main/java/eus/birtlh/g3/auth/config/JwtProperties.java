package eus.birtlh.g3.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Propiedades externas necesarias para crear y validar tokens JWT.
 *
 * <p>Se enlaza con el prefijo {@code security.jwt}. La clave de firma se recibe mediante una
 * variable de entorno y se codifica en Base64; su conversión a clave criptográfica se realiza en
 * la configuración de seguridad.</p>
 *
 * @param signingKey clave de firma JWT codificada en Base64
 * @param issuer identificador de la aplicación emisora de los tokens
 * @param accessTokenTtl duración de validez del token de acceso
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank(message = "La clave de firma JWT es obligatoria.")
        String signingKey,

        @NotBlank(message = "El emisor JWT es obligatorio.")
        String issuer,

        @NotNull(message = "La duración del token JWT es obligatoria.")
        Duration accessTokenTtl
) {

}
