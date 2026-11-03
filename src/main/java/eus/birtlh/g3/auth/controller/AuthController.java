package eus.birtlh.g3.auth.controller;

import eus.birtlh.g3.auth.dto.AuthResponse;
import eus.birtlh.g3.auth.dto.LoginRequest;
import eus.birtlh.g3.auth.service.AuthService;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone las operaciones públicas de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica un usuario mediante email y contraseña y devuelve un token de acceso JWT.
     *
     * @param request credenciales normalizadas y validadas del usuario
     * @return respuesta HTTP 200 con el token de acceso, su tipo y su duración
     */
    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponse<AuthResponse>> login(
            @Valid
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        authService.login(request),
                        "Inicio de sesión correcto."
                )
        );
    }
}
