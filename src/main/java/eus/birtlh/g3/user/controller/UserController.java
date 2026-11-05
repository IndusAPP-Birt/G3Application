package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.user.UserValidationConstraints;
import eus.birtlh.g3.user.dto.AdminCreateUserRequest;
import eus.birtlh.g3.user.dto.AdminResetUserPasswordRequest;
import eus.birtlh.g3.user.dto.AdminUpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST que expone las operaciones HTTP sobre usuarios.
 *
 * <p>Delega los casos de uso en {@link UserService} y devuelve sus resultados mediante el
 * contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtiene una página de usuarios registrados.
     *
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @return respuesta HTTP 200 con la página de usuarios y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<UserResponse>>> findAllUsers(
            @RequestParam(
                    name = "page",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_VALUE)
            @Min(
                    value = PaginationConstants.MIN_PAGE,
                    message = "La página debe ser {value} o mayor."
            )
            int page,

            @RequestParam(name = "size",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_VALUE)
            @Min(
                    value = PaginationConstants.MIN_PAGE_SIZE,
                    message = "El tamaño de página debe ser {value} o mayor."
            )
            @Max(
                    value = PaginationConstants.MAX_PAGE_SIZE,
                    message = "El tamaño de página no puede superar {value}."
            )
            int size
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getAllUsers(page, size),
                        "Usuarios obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene los roles disponibles para su selección en la API.
     *
     * @return respuesta HTTP 200 con las opciones de catálogo de roles y su marca temporal
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiSuccessResponse<List<CatalogOptionResponse>>> listAllRoles() {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getAllRoles(),
                        "Roles obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene el perfil del usuario identificado por el JWT de la petición.
     *
     * @param jwt token JWT ya validado por Spring Security
     * @return respuesta HTTP 200 con el usuario autenticado y su marca temporal
     */
    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> getAuthenticatedUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getAuthenticatedUser(jwt.getSubject()),
                        "Usuario obtenido correctamente."
                )
        );
    }

    /**
     * Obtiene un usuario por su identificador.
     *
     * @param id identificador positivo del usuario solicitado
     * @return respuesta HTTP 200 con el usuario y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> findUserById(
            @PathVariable
            @Positive (message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getUserById(id),
                        "Usuario obtenido correctamente."
                )
        );
    }

    /**
     * Obtiene una página de usuarios cuyo correo electrónico contiene el texto indicado.
     *
     * <p>El término no necesita ser una dirección de correo completa; permite buscar fragmentos
     * del nombre local, del dominio o de ambos.</p>
     *
     * @param email término parcial obligatorio con el que filtrar el correo electrónico
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @return respuesta HTTP 200 con la página de usuarios coincidentes y su marca temporal
     */
    @GetMapping("/by-email")
    public ResponseEntity<ApiSuccessResponse<PageResponse<UserResponse>>> findUsersByEmail(
            @RequestParam("email")
            @NotBlank(message = "La dirección de correo electrónico es obligatoria.")
            @Size(
                    max = UserValidationConstraints.MAX_EMAIL_LENGTH,
                    message = "La dirección de correo electrónico no puede superar los {max} caracteres."
            )
            String email,

            @RequestParam(
                    name = "page",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_VALUE
            )
            @Min(
                    value = PaginationConstants.MIN_PAGE,
                    message = "La página debe ser {value} o mayor."
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_VALUE
            )
            @Min(
                    value = PaginationConstants.MIN_PAGE_SIZE,
                    message = "El tamaño de página debe ser {value} o mayor."
            )
            @Max(
                    value = PaginationConstants.MAX_PAGE_SIZE,
                    message = "El tamaño de página no puede superar {value}."
            )
            int size
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getUsersByEmail(page, size, email),
                        "Usuarios obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de usuarios según su estado de acceso.
     *
     * @param enabled {@code true} para usuarios habilitados; {@code false} para deshabilitados
     * @param page índice de página basado en cero
     * @param size número de usuarios por página
     * @return respuesta HTTP 200 con la página de usuarios y su marca temporal
     */
    @GetMapping("/by-status")
    public ResponseEntity<ApiSuccessResponse<PageResponse<UserResponse>>> findUsersByEnabled(
            @RequestParam("enabled")
            boolean enabled,

            @RequestParam(
                    name = "page",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_VALUE
            )
            @Min(
                    value = PaginationConstants.MIN_PAGE,
                    message = "La página debe ser {value} o mayor."
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE_VALUE
            )
            @Min(
                    value = PaginationConstants.MIN_PAGE_SIZE,
                    message = "El tamaño de página debe ser {value} o mayor."
            )
            @Max(
                    value = PaginationConstants.MAX_PAGE_SIZE,
                    message = "El tamaño de página no puede superar {value}."
            )
            int size
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.getUsersByEnabled(page, size, enabled),
                        "Usuarios obtenidos correctamente."
                )
        );
    }

    /**
     * Crea un usuario a partir de una solicitud administrativa validada.
     *
     * @param request datos del usuario que se desea crear
     * @return respuesta HTTP 201 con el usuario creado y la cabecera {@code Location} del recurso
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<UserResponse>> createUser(
            @Valid
            @RequestBody AdminCreateUserRequest request
    ) {
        UserResponse user = userService.createUser(request);

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(user.id())
                        .toUri()
        ).body(
                ApiSuccessResponse.success(
                        user,
                        "Usuario creado correctamente."
                )
        );
    }

    /**
     * Actualiza los datos administrativos de un usuario, excepto su contraseña.
     *
     * @param id identificador positivo del usuario que se desea actualizar
     * @param request datos administrables que sustituyen los datos actuales del usuario
     * @return respuesta HTTP 200 con el usuario actualizado y su marca temporal
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> updateUser(
            @PathVariable
            @Positive (message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdateUserRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.updateUser(id, request),
                        "Usuario actualizado correctamente."
                )
        );
    }

    /**
     * Restablece la contraseña de un usuario a partir de una solicitud administrativa validada.
     *
     * @param id identificador positivo del usuario cuya contraseña se desea restablecer
     * @param request nueva contraseña validada
     * @return respuesta HTTP 200 con el usuario actualizado y su marca temporal
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiSuccessResponse<UserResponse>> resetUserPassword(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminResetUserPasswordRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        userService.resetUserPassword(id, request),
                        "Contraseña restablecida correctamente."
                )
        );
    }
}
