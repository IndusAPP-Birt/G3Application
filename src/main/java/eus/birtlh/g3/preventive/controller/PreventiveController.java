package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventiveService;
import eus.birtlh.g3.preventive.PreventiveStatus;
import eus.birtlh.g3.preventive.dto.AdminAssignInspectorToPreventiveRequest;
import eus.birtlh.g3.preventive.dto.PreventiveResponse;
import eus.birtlh.g3.preventive.dto.RecordPreventiveWorkRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    /**
     * Obtiene una página de preventivos asociados a un plan preventivo.
     *
     * @param preventivePlanId identificador positivo del plan preventivo con el que filtrar
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return respuesta HTTP 200 con la página de preventivos y su marca temporal
     */
    @GetMapping("/by-plan/{preventivePlanId}")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventiveResponse>>> findPreventivesByPreventivePlanId(
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
                        preventiveService.getPreventivesByPreventivePlanId(page, size, preventivePlanId),
                        "Preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de preventivos según su estado.
     *
     * @param status estado obligatorio con el que filtrar los preventivos
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return respuesta HTTP 200 con la página de preventivos y su marca temporal
     */
    @GetMapping("/by-status")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventiveResponse>>> findPreventivesByStatus(
            @RequestParam("status")
            PreventiveStatus status,

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
                        preventiveService.getPreventivesByStatus(page, size, status),
                        "Preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de preventivos asignados a un inspector.
     *
     * @param inspectorId identificador positivo del inspector con el que filtrar
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return respuesta HTTP 200 con la página de preventivos y su marca temporal
     */
    @GetMapping("/by-inspector/{inspectorId}")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventiveResponse>>> findPreventivesByAssignedInspectorId(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long inspectorId,

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
                        preventiveService.getPreventivesByAssignedInspectorId(page, size, inspectorId),
                        "Preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de preventivos cuya fecha prevista está comprendida en un rango
     * inclusivo.
     *
     * @param from fecha inicial del rango, incluida, en formato ISO-8601 ({@code yyyy-MM-dd})
     * @param to fecha final del rango, incluida, en formato ISO-8601 ({@code yyyy-MM-dd})
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return respuesta HTTP 200 con la página de preventivos y su marca temporal
     */
    @GetMapping("/by-due-date")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventiveResponse>>> findPreventivesByDueDate(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

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
                        preventiveService.getPreventivesByDueDate(
                                page, size, from, to
                        ),
                        "Preventivos obtenidos correctamente."
                )
        );
    }

    /**
     * Asigna un inspector a un preventivo pendiente.
     *
     * @param id identificador positivo del preventivo al que se asignará el inspector
     * @param request identificador validado del inspector que recibirá la asignación
     * @return respuesta HTTP 200 con el preventivo actualizado y su marca temporal
     */
    @PutMapping("/{id}/assignment")
    public ResponseEntity<ApiSuccessResponse<PreventiveResponse>> assignInspectorToPreventive(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminAssignInspectorToPreventiveRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventiveService.assignInspectorToPreventive(id, request),
                        "Inspector asignado correctamente."
                )
        );
    }

    /**
     * Inicia un preventivo que ya tiene un inspector asignado.
     *
     * @param id identificador positivo del preventivo que se desea iniciar
     * @return respuesta HTTP 200 con el preventivo iniciado y su marca temporal
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<ApiSuccessResponse<PreventiveResponse>> startPreventive(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventiveService.startPreventive(id),
                        "Preventivo iniciado correctamente."
                )
        );
    }

    /**
     * Registra o corrige el trabajo realizado en un preventivo en curso.
     *
     * @param id identificador positivo del preventivo en el que se registra el trabajo
     * @param request informe de trabajo validado que se desea registrar
     * @return respuesta HTTP 200 con el preventivo actualizado y su marca temporal
     */
    @PutMapping("/{id}/work-performed")
    public ResponseEntity<ApiSuccessResponse<PreventiveResponse>> recordWorkPerformed(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody RecordPreventiveWorkRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventiveService.recordWorkPerformed(id,  request),
                        "Trabajo registrado correctamente."
                )
        );
    }

    /**
     * Completa un preventivo en curso que tiene trabajo registrado.
     *
     * @param id identificador positivo del preventivo que se desea completar
     * @return respuesta HTTP 200 con el preventivo completado y su marca temporal
     */
    @PutMapping("/{id}/completion")
    public ResponseEntity<ApiSuccessResponse<PreventiveResponse>> completePreventive(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        preventiveService.completePreventive(id),
                        "Preventivo completado correctamente."
                )
        );
    }
}
