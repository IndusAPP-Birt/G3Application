package eus.birtlh.g3.machine.controller;

import eus.birtlh.g3.common.PaginationConstants;
import eus.birtlh.g3.common.dto.ApiSuccessResponse;
import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.MachineService;
import eus.birtlh.g3.machine.MachineStatus;
import eus.birtlh.g3.machine.dto.MachineResponse;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

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
     * Obtiene una máquina por su código patrimonial único.
     *
     * <p>El código recibido se elimina de espacios exteriores y se normaliza a mayúsculas antes
     * de delegar la búsqueda en el servicio.</p>
     *
     * @param assetCode código patrimonial obligatorio de la máquina solicitada
     * @return respuesta HTTP 200 con la máquina y su marca temporal
     */
    @GetMapping("/by-asset-code")
    public ResponseEntity<ApiSuccessResponse<MachineResponse>> findMachineByAssetCode(
            @RequestParam("assetCode")
            @NotBlank(message = "El código patrimonial es obligatorio.")
            @Size(
                    max = 50,
                    message = "El código patrimonial no puede superar los 50 caracteres."
            )
            String assetCode
    ) {
        return ResponseEntity.ok(
                ApiSuccessResponse.success(
                        machineService.getMachineByAssetCode(
                                assetCode.strip().toUpperCase(Locale.ROOT)
                        ),
                        "Máquina obtenida correctamente."
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
}
