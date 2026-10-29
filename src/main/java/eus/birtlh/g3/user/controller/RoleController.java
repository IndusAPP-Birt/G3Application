package eus.birtlh.g3.user.controller;

import eus.birtlh.g3.common.dto.ApiResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.user.Role;
import eus.birtlh.g3.user.mapper.RoleMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Expone el catálogo de roles disponibles en la aplicación.
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    /**
     * Obtiene todos los roles con su código y descripción.
     *
     * @return respuesta HTTP con el catálogo de roles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogOptionResponse>>> list() {
        List<CatalogOptionResponse> roles = Arrays.stream(Role.values())
                .map(RoleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        roles,
                        "Catálogo de roles obtenido correctamente.",
                        Instant.now()
                )
        );
    }
}
