package eus.birtlh.g3.preventive.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.PreventivePlanService;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    /**
     * Obtiene una página de planes preventivos según estén activos o inactivos.
     *
     * @param active {@code true} para los planes activos; {@code false} para los inactivos
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return respuesta HTTP 200 con la página de planes preventivos y su marca temporal
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
     * Obtiene una página de planes preventivos cuyo nombre contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de realizar una búsqueda que no
     * distingue entre mayúsculas y minúsculas.</p>
     *
     * @param name texto parcial obligatorio con el que filtrar el nombre
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return respuesta HTTP 200 con la página de planes preventivos coincidentes y su marca
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
     * Obtiene una página de planes preventivos con próxima fecha prevista dentro de un rango.
     *
     * <p>Las fechas se reciben en formato ISO {@code yyyy-MM-dd}; ambos límites se incluyen en
     * la búsqueda.</p>
     *
     * @param from fecha inicial obligatoria del rango, incluida
     * @param to fecha final obligatoria del rango, incluida
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return respuesta HTTP 200 con la página de planes preventivos y su marca temporal
     */
    @GetMapping("/by-next-due-date")
    public ResponseEntity<ApiSuccessResponse<PageResponse<PreventivePlanResponse>>> findPreventivePlansByNextDueDate(
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
                        preventivePlanService.getPreventivePlansByNextDueDate(
                                page, size, from, to
                        ),
                        "Planes preventivos obtenidos correctamente."
                )
        );
    }
}
