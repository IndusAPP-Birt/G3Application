package eus.birtlh.g3.user.service;

import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.User;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UpdateUserRequest;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

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
     * Obtiene un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo electrónico del usuario; se normaliza antes de buscar
     * @return usuario encontrado
     * @throws UserNotFoundException si no existe un usuario con la dirección de correo electrónico indicada
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Obtiene los usuarios que tienen asignado un rol determinado.
     *
     * @param role rol por el que se va a filtrar
     * @return lista de usuarios con el rol indicado
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role);
    }

    /**
     * Obtiene los usuarios según su estado de habilitación.
     *
     * @param enabled estado de habilitación por el que se va a filtrar
     * @return lista de usuarios con el estado indicado
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByEnabled(boolean enabled) {
        return userRepository.findAllByEnabled(enabled);
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

    /**
     * Actualiza los datos de un usuario.
     *
     * @param id identificador del usuario que se va a actualizar
     * @param request datos actualizados del usuario
     * @return usuario actualizado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     * @throws EmailAlreadyInUseException si la dirección de correo electrónico ya está asignada a otro usuario
     * @throws IllegalStateException si se intenta rebajar el rol de un administrador
     */
    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = this.getUserById(id);

        userRepository.findByEmail(request.email())
                .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                .ifPresent(existingUser -> {
                    throw new EmailAlreadyInUseException();
                });

        user.changeName(request.name());
        user.changeLastName(request.lastName());
        user.changeEmail(request.email());
        user.changeRole(request.role());

        return userRepository.save(user);
    }

    /**
     * Habilita a un usuario.
     *
     * @param id identificador del usuario que se va a habilitar
     * @return usuario habilitado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     */
    @Transactional
    public User enableUser(Long id) {
        User user = this.getUserById(id);
        user.enable();

        return userRepository.save(user);
    }

    /**
     * Deshabilita a un usuario.
     *
     * @param id identificador del usuario que se va a deshabilitar
     * @return usuario deshabilitado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     * @throws IllegalStateException si se intenta deshabilitar a un administrador
     */
    @Transactional
    public User disableUser(Long id) {
        User user = this.getUserById(id);
        user.disable();

        return userRepository.save(user);
    }
}
