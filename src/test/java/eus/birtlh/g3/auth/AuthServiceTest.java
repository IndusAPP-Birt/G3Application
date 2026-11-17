package eus.birtlh.g3.auth;

import eus.birtlh.g3.auth.dto.AuthResponse;
import eus.birtlh.g3.auth.dto.LoginRequest;
import eus.birtlh.g3.auth.service.AuthService;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserService;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtDecoder jwtDecoder;

    /** Comprueba que un usuario habilitado recibe un JWT con su identidad y rol. */
    @DisplayName("Un usuario habilitado obtiene un JWT con su correo y rol")
    @Test
    void autenticaUsuarioHabilitadoYEmiteTokenConSusClaims() {
        UserResponse user = createUser("auth.enabled@test.local");

        AuthResponse response = authService.login(new LoginRequest(user.email(), "Password123!"));
        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertThat(response.tokenType()).as("El tipo de token es Bearer").isEqualTo("Bearer");
        assertThat(response.expiresIn()).as("El token tiene una duración positiva").isPositive();
        assertThat(jwt.getSubject()).as("El sujeto del token es el correo del usuario")
                .isEqualTo(user.email());
        assertThat(jwt.getClaimAsString("role")).as("El token incluye el rol del usuario")
                .isEqualTo(Role.ROLE_INSPECTOR.name());
    }

    /** Comprueba que un usuario deshabilitado no puede autenticarse ni recibir un token. */
    @DisplayName("Un usuario deshabilitado no puede iniciar sesión")
    @Test
    void rechazaAutenticacionDeUsuarioDeshabilitado() {
        UserResponse user = createUser("auth.disabled@test.local");
        userService.updateUser(user.id(), new UpdateUserRequest(
                user.name(), user.lastName(), user.email(), Role.ROLE_INSPECTOR, false
        ));

        assertThatThrownBy(() -> authService.login(new LoginRequest(user.email(), "Password123!")))
                .as("Un usuario deshabilitado no recibe un token")
                .isInstanceOf(AuthenticationException.class);
    }

    private UserResponse createUser(String email) {
        return userService.createUser(new CreateUserRequest(
                "Usuario", "Autenticación", email, "Password123!", Role.ROLE_INSPECTOR
        ));
    }
}
