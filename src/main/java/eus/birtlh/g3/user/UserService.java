package eus.birtlh.g3.user;

import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.user.dto.AdminCreateUserRequest;
import eus.birtlh.g3.user.dto.AdminUpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.exception.EmailAlreadyInUseException;
import eus.birtlh.g3.user.exception.UserNotFoundException;
import eus.birtlh.g3.user.mapper.RoleMapper;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
     * Obtiene todos los usuarios registrados como DTOs de respuesta.
     *
     * @return lista de usuarios públicos; puede estar vacía
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

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
     * Actualiza completamente un usuario con datos administrativos validados.
     *
     * <p>Comprueba que el correo electrónico no pertenezca a otro usuario, codifica la nueva
     * contraseña y actualiza sus datos, rol y estado de acceso. Fuerza la sincronización con la
     * base de datos antes de construir la respuesta para incluir la fecha {@code updatedAt}
     * actualizada.</p>
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
        user.changePasswordHash(passwordEncoder.encode(request.password()));
        user.changeRole(request.role());

        if (request.enabled()) {
            user.enable();
        } else  {
            user.disable();
        }

        userRepository.flush();

        return UserMapper.toResponse(user);
    }
}
