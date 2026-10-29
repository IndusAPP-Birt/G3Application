package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.UserValidationConstraints;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.List;

// TODO: Asignar permisos por rol a todos los endpoints de este controlador.
//  Temporalmente están disponibles sin restricciones de rol.
/**
 * Expone las operaciones HTTP relacionadas con los usuarios.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtiene todos los usuarios registrados cuando no se especifican filtros soportados.
     *
     * @return respuesta HTTP con la lista de usuarios
     */
    @GetMapping(params = {"!email", "!role", "!enabled"})
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        users,
                        "Usuarios obtenidos correctamente.",
                        Instant.now())
        );
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return respuesta HTTP con el usuario solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(
            @PathVariable
            @Positive(message = "El id debe ser positivo.")
            Long id
    ) {
        UserResponse user = UserMapper.toResponse(userService.getUserById(id));

        return ResponseEntity.ok(
                new ApiResponse<>(
                        user,
                        "Usuario obtenido correctamente.",
                        Instant.now()
                )
        );
    }

    /**
     * Obtiene un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo electrónico del usuario
     * @return respuesta HTTP con el usuario solicitado
     */
    @GetMapping(params = {"email", "!role", "!enabled"})
    public ResponseEntity<ApiResponse<UserResponse>> findByEmail(
            @RequestParam("email")
            @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
            @Size(
                    max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                    message = "La dirección de correo electrónico no puede superar los {max} caracteres.")
            @Pattern(
                    regexp = UserValidationConstraints.EMAIL_REGEX,
                    message = "La dirección de correo electrónico debe tener un formato válido."
            )
            String email
    ) {
        UserResponse user = UserMapper.toResponse(userService.getUserByEmail(email));

        return ResponseEntity.ok(
                new ApiResponse<>(
                        user,
                        "Usuario obtenido correctamente",
                        Instant.now()
                )
        );
    }

    /**
     * Obtiene los usuarios que tienen asignado un rol determinado.
     *
     * @param role rol por el que se va a filtrar
     * @return respuesta HTTP con la lista de usuarios que tienen el rol indicado
     */
    @GetMapping(params = {"!email", "role", "!enabled"})
    public ResponseEntity<ApiResponse<List<UserResponse>>> findByRole(
            @RequestParam("role")
            @NotNull(message = "El rol es obligatorio.")
            Role role
    ) {
        List<UserResponse> users = userService.getUsersByRole(role)
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        users,
                        "Usuarios obtenidos correctamente.",
                        Instant.now()
                )
        );
    }

    /**
     * Obtiene los usuarios según su estado de habilitación.
     *
     * @param enabled estado de habilitación por el que se va a filtrar
     * @return respuesta HTTP con la lista de usuarios que tienen el estado indicado
     */
    @GetMapping(params = {"!email", "!role", "enabled"})
    public ResponseEntity<ApiResponse<List<UserResponse>>> findByEnabled(
            @RequestParam("enabled")
            boolean enabled
    ) {
        List<UserResponse> users = userService.getUsersByEnabled(enabled)
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        users,
                        "Usuarios obtenidos correctamente.",
                        Instant.now()
                )
        );
    }


    /**
     * Crea un nuevo usuario.
     *
     * @param request datos necesarios para crear el usuario
     * @return respuesta HTTP con el usuario creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid
            @RequestBody CreateUserRequest request
    ) {
        UserResponse user = UserMapper.toResponse(
                userService.createUser(request)
        );

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(user.id())
                        .toUri()
        ).body(
                new ApiResponse<>(
                        user,
                        "Usuario creado correctamente.",
                        Instant.now()
                )
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable
            @Positive(message = "El id debe ser positivo.")
            Long id,

            @Valid
            @RequestBody UpdateUserRequest request
    ) {
        UserResponse user = UserMapper.toResponse(
                userService.updateUser(id, request)
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        user,
                        "Usuario actualizado correctamente.",
                        Instant.now()
                )
        );
    }
}
