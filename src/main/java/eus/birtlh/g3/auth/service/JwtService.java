package eus.birtlh.g3.auth.service;

import eus.birtlh.g3.auth.config.JwtProperties;
import eus.birtlh.g3.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio que genera los tokens de acceso JWT emitidos tras una autenticación correcta.
 *
 * <p>Incluye el email como sujeto, el rol como claim, el emisor y las marcas temporales de
 * emisión y caducidad. La firma se delega en el {@link JwtEncoder} configurado con HS256.</p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    /**
     * Genera un token de acceso firmado para el usuario autenticado.
     *
     * @param principal identidad autenticada de la que se obtienen el email y el rol
     * @return representación compacta del JWT firmado
     * @throws JwtEncodingException si no es posible codificar o firmar el token
     */
    public String generateAccessToken(UserPrincipal principal) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(principal.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("role", principal.getRole().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
