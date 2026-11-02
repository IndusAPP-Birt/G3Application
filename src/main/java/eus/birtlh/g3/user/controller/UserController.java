package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.user.dto.AdminCreateUserRequest;
import eus.birtlh.g3.user.dto.AdminUpdateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.UserService;
import jakarta.validation.Valid;
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
 * contrato estándar {@link ApiResponse}.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return respuesta HTTP 200 con la lista de usuarios y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.getAllUsers(),
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
    public ResponseEntity<ApiResponse<List<CatalogOptionResponse>>> listAllRoles() {
        return ResponseEntity.ok(
                ApiResponse.success(
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
    public ResponseEntity<ApiResponse<UserResponse>> findUserById(
            @PathVariable
            @Positive (message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
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
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
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
                ApiResponse.success(
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
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable
            @Positive (message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdateUserRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.updateUser(id, request),
                        "Usuario actualizado correctamente."
                )
        );
    }
}
