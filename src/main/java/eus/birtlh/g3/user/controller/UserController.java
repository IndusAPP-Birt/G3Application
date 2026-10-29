package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.user.dto.CreateUserRequest;
import eus.birtlh.g3.user.dto.UserResponse;
import eus.birtlh.g3.user.mapper.UserMapper;
import eus.birtlh.g3.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
}
