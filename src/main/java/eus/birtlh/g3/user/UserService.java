package eus.birtlh.g3.user;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.user.dto.AdminCreateUserRequest;
import eus.birtlh.g3.user.dto.AdminResetUserPasswordRequest;
import eus.birtlh.g3.user.dto.AdminUpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.mapper.RoleMapper;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de aplicación que coordina los casos de uso de usuarios.
 *
 * <p>Obtiene y persiste entidades {@link User}, y las transforma en DTOs públicos antes de
 * devolverlas a la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los roles disponibles como opciones de catálogo públicas.
     *
     * @return lista de roles disponibles con su código y descripción
     */
    public List<CatalogOptionResponse> getAllRoles() {
        return Arrays.stream(Role.values())
                .map(RoleMapper::toResponse)
                .toList();
    }

    /**
     * Obtiene una página de usuarios registrados como DTOs de respuesta.
     *
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @return página de usuarios públicos; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                users.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    /**
     * Obtiene un usuario por su identificador como DTO de respuesta.
     *
     * @param id identificador del usuario solicitado
     * @return DTO público del usuario encontrado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario con " + id + " no encontrado."
                ));

        return UserMapper.toResponse(user);
    }

    /**
     * Obtiene el usuario identificado por el JWT de la petición autenticada.
     *
     * @param email email normalizado incluido como sujeto del token JWT
     * @return DTO público del usuario autenticado
     * @throws UserNotFoundException si no existe un usuario para el sujeto autenticado
     */
    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario autenticado no encontrado."
                ));

        return UserMapper.toResponse(user);
    }

    /**
     * Obtiene una página de usuarios cuyo correo electrónico contiene el texto indicado.
     *
     * <p>Normaliza el término de búsqueda antes de consultar el repositorio y conserva el orden
     * ascendente por identificador.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @param email término parcial con el que filtrar el correo electrónico
     * @return página de usuarios coincidentes; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByEmail(
            int page,
            int size,
            String email
    ) {
        Page<User> users = userRepository.findByEmailContainingIgnoreCase(
                email.strip().toLowerCase(Locale.ROOT),
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                users.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    /**
     * Obtiene una página de usuarios según su estado de acceso.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @param enabled {@code true} para usuarios habilitados; {@code false} para deshabilitados
     * @return página de usuarios con el estado indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByEnabled(
            int page,
            int size,
            boolean enabled
    ) {
        Page<User> users = userRepository.findByEnabled(
                enabled,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                users.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    /**
     * Obtiene una página de usuarios según el rol que tienen asignado.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @param role rol con el que filtrar los usuarios
     * @return página de usuarios con el rol indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByRole(
            int page,
            int size,
            Role role
    ) {
        Page<User> users = userRepository.findByRole(
                role,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                users.getContent()
                        .stream()
                        .map(UserMapper::toResponse)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    /**
     * Crea un usuario a partir de los datos validados de una solicitud administrativa.
     *
     * <p>Codifica la contraseña antes de persistirla y devuelve el usuario creado sin exponer su
     * hash.</p>
     *
     * @param request datos normalizados y validados del usuario que se desea crear
     * @return DTO público del usuario creado
     * @throws EmailAlreadyInUseException si ya existe un usuario con el correo electrónico indicado
     */
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(
                    "El email " + request.email() + " ya existe."
            );
        }

        User user = userRepository.save(
                new User(
                        request.name(),
                        request.lastName(),
                        request.email(),
                        passwordEncoder.encode(request.password()),
                        request.role()
                )
        );

        return UserMapper.toResponse(user);
    }

    /**
     * Actualiza los datos administrativos de un usuario, excepto su contraseña.
     *
     * <p>Comprueba que el correo electrónico no pertenezca a otro usuario y actualiza sus datos,
     * rol y estado de acceso. La contraseña se actualiza mediante un caso de uso independiente.
     * Fuerza la sincronización con la base de datos antes de construir la respuesta para incluir
     * la fecha {@code updatedAt} actualizada.</p>
     *
     * @param id identificador del usuario que se desea actualizar
     * @param request datos normalizados y validados que sustituyen los datos administrables
     * @return DTO público del usuario actualizado
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     * @throws EmailAlreadyInUseException si el correo electrónico pertenece a otro usuario
     */
    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario con " + id + " no encontrado."
                ));

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailAlreadyInUseException(
                    "El email " + request.email() + " ya existe."
            );
        }

        user.changeName(request.name());
        user.changeLastName(request.lastName());
        user.changeEmail(request.email());
        user.changeRole(request.role());

        if (request.enabled()) {
            user.enable();
        } else  {
            user.disable();
        }

        userRepository.flush();

        return UserMapper.toResponse(user);
    }

    /**
     * Restablece la contraseña de un usuario con una solicitud administrativa validada.
     *
     * <p>Codifica la nueva contraseña antes de almacenarla y sincroniza la modificación para que
     * la respuesta incluya la fecha {@code updatedAt} actualizada.</p>
     *
     * @param id identificador del usuario cuya contraseña se desea restablecer
     * @param request nueva contraseña validada
     * @return DTO público del usuario con la contraseña actualizada, sin exponer su hash
     * @throws UserNotFoundException si no existe un usuario con el identificador indicado
     */
    @Transactional
    public UserResponse resetUserPassword(
            Long id,
            AdminResetUserPasswordRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario con " + id + " no encontrado."
                ));

        user.changePasswordHash(passwordEncoder.encode(request.password()));

        userRepository.flush();

        return UserMapper.toResponse(user);
    }
}
