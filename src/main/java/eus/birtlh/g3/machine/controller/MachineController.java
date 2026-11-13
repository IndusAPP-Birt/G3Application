package eus.birtlh.g3.machine.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.CatalogOptionResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.MachineStatus;
import eus.birtlh.g3.machine.dto.AdminCreateMachineRequest;
import eus.birtlh.g3.machine.dto.AdminUpdateAssetCodeRequest;
import eus.birtlh.g3.machine.dto.AdminUpdateMachineRequest;
import eus.birtlh.g3.machine.dto.MachineResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST que expone las operaciones HTTP sobre máquinas industriales.
 *
 * <p>Delega los casos de uso en {@link MachineService} y devuelve sus resultados mediante el
 * contrato estándar {@link ApiSuccessResponse}.</p>
 */
@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    /**
     * Obtiene los estados operativos disponibles para su selección en la API.
     *
     * @return respuesta HTTP 200 con las opciones de catálogo de estados y su marca temporal
     */
    @GetMapping("/statuses")
    public ResponseEntity<ApiSuccessResponse<List<CatalogOptionResponse>>> listAllStatuses() {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        machineService.getAllStatuses(),
                        "Estados de máquinas obtenidos correctamente."
                )
        );
    }

    /**
     * Obtiene una página de máquinas registradas.
     *
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @return respuesta HTTP 200 con la página de máquinas y su marca temporal
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<MachineResponse>>> findAllMachines(
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
                        machineService.getAllMachines(page, size),
                        "Máquinas obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una máquina por su identificador.
     *
     * @param id identificador positivo de la máquina solicitada
     * @return respuesta HTTP 200 con la máquina y su marca temporal
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<MachineResponse>> findMachineById(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        machineService.getMachineById(id),
                        "Máquina obtenida correctamente."
                )
        );
    }

    /**
     * Obtiene una página de máquinas cuyo código patrimonial contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de delegar la búsqueda, que no
     * distingue entre mayúsculas y minúsculas.</p>
     *
     * @param assetCode texto parcial obligatorio con el que filtrar el código patrimonial
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @return respuesta HTTP 200 con la página de máquinas coincidentes y su marca temporal
     */
    @GetMapping("/by-asset-code")
    public ResponseEntity<ApiSuccessResponse<PageResponse<MachineResponse>>> findMachinesByAssetCode(
            @RequestParam("assetCode")
            @NotBlank(message = "El código patrimonial es obligatorio.")
            @Size(
                    min = 2,
                    max = 50,
                    message = "El código patrimonial debe tener entre 2 y 50 caracteres."
            )
            String assetCode,

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
                        machineService.getMachinesByAssetCode(page, size, assetCode.strip()),
                        "Máquinas obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una página de máquinas cuyo número de serie contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de delegar la búsqueda, que no
     * distingue entre mayúsculas y minúsculas.</p>
     *
     * @param serialNumber texto parcial obligatorio con el que filtrar el número de serie
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @return respuesta HTTP 200 con la página de máquinas coincidentes y su marca temporal
     */
    @GetMapping("/by-serial-number")
    public ResponseEntity<ApiSuccessResponse<PageResponse<MachineResponse>>> findMachinesBySerialNumber(
            @RequestParam("serialNumber")
            @NotBlank(message = "El número de serie es obligatorio.")
            @Size(
                    min = 2,
                    max = 100,
                    message = "El número de serie, debe tener entre 2 y 100 caracteres."
            )
            String serialNumber,

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
                        machineService.getMachinesBySerialNumber(page, size, serialNumber.strip()),
                        "Máquinas obtenidas correctamente."
                )
        );
    }

    /**
     * Obtiene una página de máquinas según su estado operativo.
     *
     * @param status estado obligatorio con el que filtrar las máquinas
     * @param page índice de página basado en cero
     * @param size número de máquinas por página
     * @return respuesta HTTP 200 con la página de máquinas y su marca temporal
     */
    @GetMapping("/by-status")
    public ResponseEntity<ApiSuccessResponse<PageResponse<MachineResponse>>> findMachinesByStatus(
        @RequestParam("status")
        MachineStatus status,

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
                        machineService.getMachinesByStatus(page, size, status),
                        "Máquinas obtenidas correctamente."
                )
        );
    }

    /**
     * Registra una máquina a partir de una solicitud administrativa validada.
     *
     * @param request datos de la máquina que se desea registrar
     * @return respuesta HTTP 201 con la máquina creada y la cabecera {@code Location} del recurso
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<MachineResponse>> createMachine(
            @Valid
            @RequestBody AdminCreateMachineRequest request
    ) {
        MachineResponse machine = machineService.createMachine(request);

        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(machine.id())
                        .toUri()
        ).body(
                ApiSuccessResponse.success(
                        machine,
                        "Máquina creada correctamente."
                )

        );
    }

    /**
     * Actualiza los datos administrativos ordinarios de una máquina.
     *
     * <p>El código patrimonial se corrige mediante un caso de uso independiente y la fecha de
     * puesta en servicio permanece inmutable.</p>
     *
     * @param id identificador positivo de la máquina que se desea actualizar
     * @param request datos actualizables que sustituyen los datos actuales de la máquina
     * @return respuesta HTTP 200 con la máquina actualizada y su marca temporal
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<MachineResponse>> updateMachine(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdateMachineRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        machineService.updateMachine(id, request),
                        "Máquina actualizada correctamente."
                )
        );
    }

    /**
     * Corrige el código patrimonial de una máquina mediante un caso de uso específico.
     *
     * @param id identificador positivo de la máquina cuyo código se desea corregir
     * @param request solicitud con el nuevo código patrimonial normalizado y validado
     * @return respuesta HTTP 200 con la máquina actualizada y su marca temporal
     */
    @PutMapping("/{id}/asset-code")
    public ResponseEntity<ApiSuccessResponse<MachineResponse>> updateMachineAssetCode(
            @PathVariable
            @Positive(message = "El identificador debe ser mayor que cero.")
            Long id,

            @Valid
            @RequestBody AdminUpdateAssetCodeRequest request
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        machineService.updateMachineAssetCode(id, request),
                        "Código patrimonial de la máquina actualizado correctamente."
                )
        );
    }
}
