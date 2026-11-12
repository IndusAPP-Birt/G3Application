package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventivePlanAssignmentService;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.AdminUpdatePreventivePlanAssignmentRequest;
import eus.birtlh.g3.preventive.dto.PreventivePlanAssignmentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST que expone las operaciones HTTP sobre asignaciones de plantillas preventivas
 * a máquinas.
 *
 * <p>Delega los casos de uso en {@link PreventivePlanAssignmentService} y devuelve sus
 * resultados mediante el contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/preventive-plan-assignments")
@RequiredArgsConstructor
public class PreventivePlanAssignmentController {

    private final PreventivePlanAssignmentService preventivePlanAssignmentService;

    /**
     * Obtiene una página de asignaciones de planes preventivos registradas.
     *
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @return respuesta HTTP 200 con la página de asignaciones y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanAssignmentResponse>>> findAllPreventivePlanAssignments(
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
                        preventivePlanAssignmentService.getAllPreventivePlanAssignments(page, size),
                        "Asignaciones de planes preventivos obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una asignación de plan preventivo por su identificador.
     *
     * @param id identificador positivo de la asignación solicitada
     * @return respuesta HTTP 200 con la asignación y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<PreventivePlanAssignmentResponse>> findPreventivePlanAssignmentById(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventivePlanAssignmentService.getPreventivePlanAssignmentById(id),
                        "Asignación de plan preventivo obtenida correctamente."
                )
        );
    }

    /**
     * Obtiene una página de asignaciones preventivas según su estado de activación.
     *
     * @param active {@code true} para las asignaciones activas; {@code false} para las inactivas
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @return respuesta HTTP 200 con la página de asignaciones y su marca temporal
     */
    @GetMapping("/by-status")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanAssignmentResponse>>> findPreventivePlanAssignmentsByActive(
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
                        preventivePlanAssignmentService.getPreventivePlanAssignmentsByActive(page, size, active),
                        "Asignaciones de planes preventivos obtenidas correctamente."                )
        );
    }

    /**
     * Obtiene una página de asignaciones preventivas configuradas para una máquina.
     *
     * @param machineId identificador positivo de la máquina con la que filtrar
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @return respuesta HTTP 200 con la página de asignaciones y su marca temporal
     */
    @GetMapping("/by-machine/{machineId}")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanAssignmentResponse>>> findPreventivePlanAssignmentsByMachineId(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long machineId,

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
                        preventivePlanAssignmentService.getPreventivePlanAssignmentsByMachineId(page, size, machineId),
                        "Asignaciones de planes preventivos obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una página de asignaciones configuradas a partir de una plantilla preventiva.
     *
     * @param preventivePlanId identificador positivo de la plantilla con la que filtrar
     * @param page índice de página basado en cero
     * @param size número de asignaciones por página
     * @return respuesta HTTP 200 con la página de asignaciones y su marca temporal
     */
    @GetMapping("/by-preventive-plan/{preventivePlanId}")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanAssignmentResponse>>> findPreventivePlanAssignmentsByPreventivePlanId(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long preventivePlanId,

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
                        preventivePlanAssignmentService.getPreventivePlanAssignmentsByPreventivePlanId(page, size, preventivePlanId),
                        "Asignaciones de planes preventivos obtenidas correctamente."
                )
        );
    }

    /**
     * Asigna una plantilla preventiva a una máquina con su planificación inicial.
     *
     * @param request datos validados de la plantilla, máquina y calendario de la asignación
     * @return respuesta HTTP 201 con la asignación creada, su marca temporal y la cabecera
     * {@code Location} del recurso
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<PreventivePlanAssignmentResponse>> createPreventivePlanAssignment(
            @Valid
            @RequestBody AdminCreatePreventivePlanAssignmentRequest request
    ) {
        PreventivePlanAssignmentResponse preventivePlanAssignment = preventivePlanAssignmentService.createPreventivePlanAssignment(request);

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(preventivePlanAssignment.id())
                        .toUri()
        ).body(
                ApiSuccessResponse.success(
                        preventivePlanAssignment,
                        "Asignación de plan preventivo creada correctamente."
                )
        );
    }

    /**
     * Actualiza el calendario y la activación de una asignación preventiva existente.
     *
     * @param id identificador positivo de la asignación que se desea actualizar
     * @param request datos validados con los que actualizar el calendario y la activación
     * @return respuesta HTTP 200 con la asignación actualizada y su marca temporal
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<PreventivePlanAssignmentResponse>> updatePreventivePlanAssignment(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdatePreventivePlanAssignmentRequest request
    ) {
        PreventivePlanAssignmentResponse preventivePlanAssignment = preventivePlanAssignmentService.updatePreventivePlanAssignment(id, request);

        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventivePlanAssignment,
                        "Asignación de plan preventivo actualizada correctamente."
                )
        );
    }
}
