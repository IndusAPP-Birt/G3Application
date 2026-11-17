package eus.birtlh.g3.user;

import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.ResetUserPasswordRequest;
import eus.birtlh.g3.user.dto.UpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Comprueba que no se puede registrar dos veces la misma dirección de correo. */
    @DisplayName("Rechaza crear un usuario con un correo ya registrado")
    @Test
    void rechazaCreacionConCorreoDuplicado() {
        createUser("duplicate@test.local", "Primero");

        assertThatThrownBy(() -> createUser("duplicate@test.local", "Segundo"))
                .as("El segundo usuario no puede reutilizar el correo")
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessage("El email duplicate@test.local ya existe.");
        assertThat(userRepository.findByEmail("duplicate@test.local"))
                .as("El primer usuario se conserva")
                .isPresent();
    }

    /** Comprueba que un usuario no puede apropiarse del correo de otro durante una actualización. */
    @DisplayName("Rechaza actualizar un usuario con el correo de otro")
    @Test
    void rechazaActualizacionConCorreoDeOtroUsuario() {
        UserResponse first = createUser("first@test.local", "Primero");
        UserResponse second = createUser("second@test.local", "Segundo");

        assertThatThrownBy(() -> userService.updateUser(
                second.id(),
                new UpdateUserRequest("Usuario", "Segundo", first.email(), Role.ROLE_INSPECTOR, true)
        ))
                .as("El correo de otro usuario no puede reutilizarse")
                .isInstanceOf(EmailAlreadyInUseException.class)
                .hasMessage("El email first@test.local ya existe.");
        assertThat(userRepository.findById(second.id()).orElseThrow().getEmail())
                .as("El segundo usuario conserva su correo original")
                .isEqualTo(second.email());
    }

    /** Comprueba que el restablecimiento sustituye el hash y deja inválida la contraseña anterior. */
    @DisplayName("Restablecer la contraseña invalida la anterior")
    @Test
    void restableceLaContrasenaConUnNuevoHash() {
        UserResponse user = createUser("password@test.local", "Contraseña");
        String previousPassword = "Password123!";
        String newPassword = "NewPassword123!";

        userService.resetUserPassword(user.id(), new ResetUserPasswordRequest(newPassword));
        User persistedUser = userRepository.findById(user.id()).orElseThrow();

        assertThat(passwordEncoder.matches(newPassword, persistedUser.getPasswordHashForAuthentication()))
                .as("La nueva contraseña coincide con el hash persistido")
                .isTrue();
        assertThat(passwordEncoder.matches(previousPassword, persistedUser.getPasswordHashForAuthentication()))
                .as("La contraseña anterior deja de coincidir")
                .isFalse();
    }

    private UserResponse createUser(String email, String lastName) {
        return userService.createUser(new CreateUserRequest(
                "Usuario", lastName, email, "Password123!", Role.ROLE_INSPECTOR
        ));
    }
}
