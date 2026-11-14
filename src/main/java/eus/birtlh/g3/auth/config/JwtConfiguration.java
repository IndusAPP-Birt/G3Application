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
 * <p>Se confía en que la clave recibida mediante {@code JWT_SIGNING_KEY} fue generada previamente
 * con 32 bytes aleatorios y está codificada en Base64. Por esta decisión no se añade una validación
 * propia de longitud ni formato antes de convertirla en una clave HMAC.</p>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    /**
     * Convierte la clave de firma configurada en una clave HMAC utilizable por JWT.
     *
     * @param properties propiedades externas de JWT
     * @return clave simétrica de firma y validación
     * @throws IllegalArgumentException si la clave configurada no contiene Base64 válido
     */
    @Bean
    public SecretKey jwtSigningKey(JwtProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.signingKey());

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
