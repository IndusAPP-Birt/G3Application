package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventivePlanService;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone las operaciones HTTP sobre planes preventivos.
 *
 * <p>Delega los casos de uso en {@link PreventivePlanService} y devuelve sus resultados mediante
 * el contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/preventive-plans")
@RequiredArgsConstructor
public class PreventivePlanController {

    private final PreventivePlanService preventivePlanService;

    // TODO: Añadir GET /by-status?active={true|false} para filtrar planes activos o inactivos.
    // TODO: Añadir GET /by-name?name={texto} para buscar planes por nombre parcial.
    // TODO: Añadir GET /by-next-due-date?from={fecha}&to={fecha} para consultar la planificación.

    /**
     * Obtiene una página de planes preventivos registrados.
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return respuesta HTTP 200 con la página de planes preventivos y su marca temporal
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
     * Obtiene un plan preventivo por su identificador.
     *
     * @param id identificador positivo del plan preventivo solicitado
     * @return respuesta HTTP 200 con el plan preventivo y su marca temporal
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
     * Obtiene una página de planes preventivos asociados a una máquina.
     *
     * @param machineId identificador positivo de la máquina con la que filtrar
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return respuesta HTTP 200 con la página de planes preventivos y su marca temporal
     */
    @GetMapping("/by-machine/{machineId}")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanResponse>>> findPreventivePlansByMachineId(
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
                        preventivePlanService.getPreventivePlansByMachineId(page, size, machineId),
                        "Planes preventivos obtenidos correctamente."
                )
        );
    }
}
