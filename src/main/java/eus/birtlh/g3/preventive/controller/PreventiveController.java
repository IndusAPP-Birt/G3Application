package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventiveService;
import eus.birtlh.g3.preventive.dto.PreventiveResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone las operaciones HTTP sobre ejecuciones preventivas.
 *
 * <p>Delega los casos de uso en {@link PreventiveService} y devuelve sus resultados mediante el
 * contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/preventives")
@RequiredArgsConstructor
public class PreventiveController {

    private final PreventiveService preventiveService;

    // TODO: Añadir GET /by-plan/{planId} para consultar las ejecuciones de un plan preventivo.
    // TODO: Añadir GET /by-status?status={estado} para filtrar preventivos por estado.
    // TODO: Añadir GET /by-inspector/{inspectorId} para consultar los preventivos asignados a un
    //       inspector.
    // TODO: Añadir GET /by-due-date?from={fecha}&to={fecha} para consultar preventivos por fecha
    //       prevista.
    // TODO: Añadir PUT /{id}/assignment para asignar un inspector a un preventivo.
    // TODO: Añadir PUT /{id}/start para iniciar un preventivo asignado.
    // TODO: Añadir PUT /{id}/work-performed para registrar el trabajo realizado.
    // TODO: Añadir PUT /{id}/completion para completar un preventivo con trabajo registrado.

    /**
     * Obtiene una página de preventivos registrados.
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return respuesta HTTP 200 con la página de preventivos y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventiveResponse>>> findAllPreventives(
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
                        preventiveService.getAllPreventives(page, size),
                        "Preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene un preventivo por su identificador.
     *
     * @param id identificador positivo del preventivo solicitado
     * @return respuesta HTTP 200 con el preventivo y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<PreventiveResponse>> findPreventiveById(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventiveService.getPreventiveById(id),
                        "Preventivo obtenido correctamente."
                )
        );
    }
}
