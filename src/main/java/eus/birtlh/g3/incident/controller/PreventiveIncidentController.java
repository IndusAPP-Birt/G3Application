package eus.birtlh.g3.incident.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.incident.IncidentService;
import eus.birtlh.g3.incident.dto.CreateIncidentRequest;
import eus.birtlh.g3.incident.dto.IncidentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controlador REST que expone las consultas y el alta de incidencias asociadas a preventivos.
 *
 * <p>Delega en {@link IncidentService} los casos de uso de incidencias y devuelve sus resultados
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
                        incidentService.getIncidentsByPreventiveId(page, size, preventiveId),
                        "Incidencias obtenidas correctamente."
                )
        );
    }

    /**
     * Comunica una incidencia detectada durante la ejecución de un preventivo.
     *
     * <p>El inspector se identifica mediante el sujeto del JWT. El servicio comprueba que sea el
     * inspector asignado y que el preventivo esté en progreso. La respuesta incluye HTTP 201 y la
     * cabecera {@code Location} del recurso creado.</p>
     *
     * @param preventiveId identificador positivo del preventivo en el que se detecta la incidencia
     * @param request descripción y gravedad validadas de la incidencia
     * @param jwt token JWT del inspector autenticado
     * @return respuesta HTTP 201 con la incidencia creada, su marca temporal y su ubicación
     */
    @PostMapping("/{preventiveId}/incidents")
    public ResponseEntity<ApiSuccessResponse<IncidentResponse>> createIncident(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long preventiveId,

            @Valid
            @RequestBody CreateIncidentRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        IncidentResponse incident = incidentService.createIncident(
                preventiveId,
                request,
                jwt.getSubject()
        );

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/incidents/{id}")
                        .buildAndExpand(incident.id())
                        .toUri()
        ).body(
                ApiSuccessResponse.success(
                        incident,
                        "Incidencia creada correctamente."
                )
        );
    }
}
