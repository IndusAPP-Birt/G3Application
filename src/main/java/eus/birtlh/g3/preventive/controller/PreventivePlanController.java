package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventivePlanService;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.AdminUpdatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST que expone las operaciones HTTP sobre plantillas de planes preventivos.
 *
 * <p>Delega los casos de uso en {@link PreventivePlanService} y devuelve sus resultados mediante
 * el contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/preventive-plans")
@RequiredArgsConstructor
public class PreventivePlanController {

    private final PreventivePlanService preventivePlanService;

    /**
     * Obtiene una página de plantillas preventivas registradas.
     *
     * @param page índice de página basado en cero
     * @param size número de plantillas preventivas por página
     * @return respuesta HTTP 200 con la página de plantillas y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanResponse>>> findAllPreventivePlans(
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
                        preventivePlanService.getAllPreventivePlans(page, size),
                        "Planes preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una plantilla preventiva por su identificador.
     *
     * @param id identificador positivo de la plantilla preventiva solicitada
     * @return respuesta HTTP 200 con la plantilla y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<PreventivePlanResponse>> findPreventivePlanById(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventivePlanService.getPreventivePlanById(id),
                        "Plan preventivo obtenido correctamente."
                )
        );
    }

    /**
     * Obtiene una página de plantillas preventivas según su disponibilidad para nuevas
     * asignaciones.
     *
     * @param active {@code true} para las plantillas activas; {@code false} para las inactivas
     * @param page índice de página basado en cero
     * @param size número de plantillas preventivas por página
     * @return respuesta HTTP 200 con la página de plantillas y su marca temporal
     */
    @GetMapping("/by-status")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanResponse>>> findPreventivePlansByActive(
            @RequestParam("active")
            boolean active,

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
                        preventivePlanService.getPreventivePlansByActive(page, size, active),
                        "Planes preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de plantillas preventivas cuyo nombre contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de realizar una búsqueda que no
     * distingue entre mayúsculas y minúsculas.</p>
     *
     * @param name texto parcial obligatorio con el que filtrar el nombre
     * @param page índice de página basado en cero
     * @param size número de plantillas preventivas por página
     * @return respuesta HTTP 200 con la página de plantillas coincidentes y su marca
     * temporal
     */
    @GetMapping("/by-name")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanResponse>>> findPreventivePlansByName(
            @RequestParam("name")
            @NotBlank(message = "El nombre es obligatorio")
            @Size(
                    min = 2,
                    max = 150,
                    message = "El nombre debe tener entre 2 y 150 caracteres."
            )
            String name,

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
                        preventivePlanService.getPreventivePlansByName(page, size, name),
                        "Planes preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Crea una plantilla preventiva a partir de una solicitud administrativa validada.
     *
     * @param request datos de la plantilla preventiva que se desea crear
     * @return respuesta HTTP 201 con la plantilla creada y la cabecera {@code Location} del
     * recurso
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<PreventivePlanResponse>> createPreventivePlan(
            @Valid
            @RequestBody AdminCreatePreventivePlanRequest request
    ) {
        PreventivePlanResponse preventivePlan = preventivePlanService.createPreventivePlan(request);

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(preventivePlan.id())
                        .toUri()
        ).body(
                ApiSuccessResponse.success(
                        preventivePlan,
                        "Plan preventivo creado correctamente."
                )
        );
    }

    /**
     * Actualiza completamente una plantilla preventiva existente.
     *
     * @param id identificador positivo de la plantilla preventiva que se desea actualizar
     * @param request datos validados con los que actualizar la plantilla
     * @return respuesta HTTP 200 con la plantilla actualizada y su marca temporal
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<PreventivePlanResponse>> updatePreventivePlan(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdatePreventivePlanRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventivePlanService.updatePreventivePlan(id, request),
                        "Plan preventivo actualizado correctamente."
                )
        );
    }
}
