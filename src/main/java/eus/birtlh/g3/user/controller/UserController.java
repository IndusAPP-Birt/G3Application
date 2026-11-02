package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.service.UserService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
