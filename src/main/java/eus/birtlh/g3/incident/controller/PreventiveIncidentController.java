package eus.birtlh.g3.incident.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.incident.IncidentService;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST que expone las consultas de incidencias asociadas a preventivos.
 *
 * <p>Delega en {@link IncidentService} la obtención de las incidencias y devuelve los resultados
 * mediante el contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/preventives")
@RequiredArgsConstructor
public class PreventiveIncidentController {

    private final IncidentService incidentService;

    /**
     * Obtiene una página de incidencias asociadas a un preventivo.
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @param preventiveId identificador positivo del preventivo solicitado
     * @return respuesta HTTP 200 con la página de incidencias y su marca temporal
     */
    @GetMapping("/{preventiveId}/incidents")
    public ResponseEntity<ApiSuccessResponse<PageResponse<IncidentResponse>>> findAllIncidentsByPreventiveId(
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
            int size,

            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long preventiveId
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        incidentService.getAllIncidentsByPreventiveId(page, size, preventiveId),
                        "Incidencias obtenidas correctamente."
                )
        );
    }
}
