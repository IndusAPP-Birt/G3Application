package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping(params = {"!email", "!enabled", "!role"})
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
}
