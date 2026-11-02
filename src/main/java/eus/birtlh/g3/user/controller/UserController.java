package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.user.dto.AdminCreateUserRequest;
import eus.birtlh.g3.user.dto.AdminUpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * Actualiza completamente un usuario a partir de una solicitud administrativa validada.
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
}
