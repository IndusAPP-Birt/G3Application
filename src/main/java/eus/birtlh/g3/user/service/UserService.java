package eus.birtlh.g3.user.service;

import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestiona las operaciones de negocio relacionadas con los usuarios.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios registrados
     */
    // TODO: Autorizar este caso de uso antes de exponerlo mediante un endpoint, ya que devuelve emails.
    // TODO: Definir el orden del listado y añadir paginación si el número de usuarios crece.
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return usuario encontrado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Crea y persiste un usuario tras comprobar la disponibilidad de su correo electrónico.
     *
     * <p>La contraseña recibida se codifica antes de almacenarse.</p>
     *
     * @param request datos necesarios para crear el usuario
     * @return usuario creado
     * @throws EmailAlreadyInUseException si la dirección de correo electrónico ya está en uso
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException();
        }

        return userRepository.save(
                new User(
                        request.name(),
                        request.lastName(),
                        request.email(),
                        passwordEncoder.encode(request.password()),
                        request.role()
                )
        );
    }
}
