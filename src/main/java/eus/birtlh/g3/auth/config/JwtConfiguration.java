package eus.birtlh.g3.auth.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Configura los componentes criptográficos necesarios para crear y validar tokens JWT con HS256.
 *
 * <p>La clave recibida mediante {@code JWT_SIGNING_KEY} se decodifica desde Base64 y debe contener
 * al menos 256 bits, requisito mínimo del algoritmo HS256.</p>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    /** Longitud mínima, en bytes, de una clave simétrica para HS256. */
    private static final int HS256_MINIMUM_KEY_LENGTH_BYTES = 32;

    /**
     * Convierte la clave de firma configurada en una clave HMAC utilizable por JWT.
     *
     * @param properties propiedades externas de JWT
     * @return clave simétrica de firma y validación
     * @throws IllegalArgumentException si la clave configurada no contiene Base64 válido
     * @throws IllegalStateException si la clave decodificada no alcanza los 256 bits requeridos
     *                               por HS256
     */
    @Bean
    public SecretKey jwtSigningKey(JwtProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.signingKey());

        if (keyBytes.length < HS256_MINIMUM_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "JWT_SIGNING_KEY debe contener al menos 256 bits para usar HS256."
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Crea el codificador encargado de firmar nuevos JWT con HS256.
     *
     * @param jwtSigningKey clave simétrica de firma
     * @return codificador JWT
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<SecurityContext>(jwtSigningKey)
        );
    }

    /**
     * Crea el decodificador que valida la firma, la caducidad y el emisor de los JWT recibidos.
     *
     * @param jwtSigningKey clave simétrica de validación
     * @param properties propiedades externas de JWT
     * @return decodificador JWT configurado para HS256
     */
    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey jwtSigningKey,
            JwtProperties properties
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(properties.issuer())
        );

        return decoder;
    }
}
