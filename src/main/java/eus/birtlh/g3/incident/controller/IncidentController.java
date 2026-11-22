package eus.birtlh.g3.incident.controller;

import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.incident.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
