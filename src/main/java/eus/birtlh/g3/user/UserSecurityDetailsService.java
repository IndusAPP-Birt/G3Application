package eus.birtlh.g3.user;

import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que carga usuarios para el proceso de autenticación de Spring Security.
 *
 * <p>Busca el usuario por su email y lo adapta a {@link UserPrincipal}, sin revelar si una
 * dirección concreta existe cuando no se encuentra.</p>
 */
@Service
@RequiredArgsConstructor
@NullMarked
public class UserSecurityDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Obtiene el principal de seguridad asociado al email recibido durante la autenticación.
     *
     * @param email email normalizado que identifica al usuario
     * @return principal de seguridad del usuario encontrado
     * @throws UsernameNotFoundException si no existe un usuario con el email indicado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciales inválidas."
                ));
    }
}
