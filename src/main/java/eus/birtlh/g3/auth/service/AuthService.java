package eus.birtlh.g3.auth.service;

import eus.birtlh.g3.auth.config.JwtProperties;
import eus.birtlh.g3.auth.dto.AuthResponse;
import eus.birtlh.g3.auth.dto.LoginRequest;
import eus.birtlh.g3.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.stereotype.Service;

/**
 * Servicio que coordina la autenticación mediante email y contraseña.
 *
 * <p>Delega la comprobación de credenciales en Spring Security y genera un token de acceso solo
 * después de obtener un {@link UserPrincipal} autenticado.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * Autentica las credenciales recibidas y devuelve un token de acceso JWT.
     *
     * @param request email y contraseña normalizados y validados
     * @return respuesta con el token de acceso, su tipo y su tiempo de validez en segundos
     * @throws AuthenticationException si las credenciales no son válidas o el usuario está
     *                                 deshabilitado
     * @throws JwtEncodingException si no es posible generar el token para una autenticación válida
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.email(),
                        request.password()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(principal);

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtProperties.accessTokenTtl().toSeconds()
        );
    }
}
