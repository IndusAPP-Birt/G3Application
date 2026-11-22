package eus.birtlh.g3.incident.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.incident.IncidentService;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST que expone los catálogos de incidencias.
 *
 * <p>Delega la obtención de estados y niveles de gravedad en {@link IncidentService}, y devuelve
 * sus resultados mediante el contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * Obtiene los niveles de gravedad disponibles para su selección en la API.
     *
     * @return respuesta HTTP 200 con las opciones de catálogo de gravedad y su marca temporal
     */
    @GetMapping("/severities")
    public ResponseEntity<ApiSuccessResponse<List<CatalogOptionResponse>>> listAllSeverities() {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        incidentService.getAllSeverities(),
                        "Niveles de gravedad obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene los estados de incidencias disponibles para su selección en la API.
     *
     * @return respuesta HTTP 200 con las opciones de catálogo de estados y su marca temporal
     */
    @GetMapping("/statuses")
    public ResponseEntity<ApiSuccessResponse<List<CatalogOptionResponse>>> listAllStatuses() {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        incidentService.getAllStatuses(),
                        "Estados obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de incidencias registradas.
     *
     * @param page índice de página basado en cero
     * @param size número de incidencias por página
     * @return respuesta HTTP 200 con la página de incidencias y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<IncidentResponse>>> findAllIncidents(
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
                        incidentService.getAllIncidents(page, size),
                        "Incidencias obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una incidencia por su identificador.
     *
     * @param id identificador positivo de la incidencia solicitada
     * @return respuesta HTTP 200 con la incidencia y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<IncidentResponse>> findIncidentById(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        incidentService.getIncidentById(id),
                        "Incidencia obtenida correctamente."
                )
        );
    }
}
