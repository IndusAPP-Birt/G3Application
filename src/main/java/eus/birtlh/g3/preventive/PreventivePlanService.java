package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import eus.birtlh.g3.preventive.exceptions.InvalidPreventivePlanDateRangeException;
import eus.birtlh.g3.preventive.exceptions.PreventivePlanNotFoundException;
import eus.birtlh.g3.preventive.mapper.PreventivePlanMapper;
import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Servicio de aplicación que coordina los casos de uso de planes preventivos.
 *
 * <p>Obtiene entidades {@link PreventivePlan} y las transforma en DTOs públicos antes de
 * devolverlas a la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class PreventivePlanService {

    private final PreventivePlanRepository preventivePlanRepository;

    /**
     * Obtiene una página de planes preventivos como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas. La transacción mantiene disponible la máquina asociada durante la conversión
     * del plan mediante {@link PreventivePlanMapper}.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @return página de planes preventivos públicos; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanResponse> getAllPreventivePlans(int page, int size) {
        Page<PreventivePlan> preventivePlans = preventivePlanRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlans.getContent()
                        .stream()
                        .map(PreventivePlanMapper::toResponse)
                        .toList(),
                preventivePlans.getNumber(),
                preventivePlans.getSize(),
                preventivePlans.getTotalElements(),
                preventivePlans.getTotalPages()
        );
    }

    /**
     * Obtiene un plan preventivo por su identificador como DTO de respuesta.
     *
     * @param id identificador del plan preventivo solicitado
     * @return DTO público del plan preventivo encontrado
     * @throws PreventivePlanNotFoundException si no existe un plan con el identificador indicado
     */
    @Transactional(readOnly = true)
    public PreventivePlanResponse getPreventivePlanById(Long id) {
        PreventivePlan preventivePlan = preventivePlanRepository.findById(id)
                .orElseThrow(() -> new PreventivePlanNotFoundException(
                        "Plan preventivo con " + id + " no encontrado."
                ));

        return PreventivePlanMapper.toResponse(preventivePlan);
    }

    /**
     * Obtiene una página de planes preventivos asociados a una máquina.
     *
     * <p>Incluye tanto los planes activos como los desactivados y conserva el orden ascendente
     * por identificador para que los resultados sean estables entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @param machineId identificador de la máquina con la que filtrar
     * @return página de planes preventivos de la máquina; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanResponse> getPreventivePlansByMachineId(
            int page,
            int size,
            long machineId
    ) {
        Page<PreventivePlan> preventivePlans = preventivePlanRepository.findByMachineId(
                machineId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlans.getContent()
                        .stream()
                        .map(PreventivePlanMapper::toResponse)
                        .toList(),
                preventivePlans.getNumber(),
                preventivePlans.getSize(),
                preventivePlans.getTotalElements(),
                preventivePlans.getTotalPages()
        );
    }

    /**
     * Obtiene una página de planes preventivos filtrada por su estado de activación.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @param active {@code true} para los planes activos; {@code false} para los inactivos
     * @return página de planes preventivos con el estado solicitado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanResponse> getPreventivePlansByActive(
            int page,
            int size,
            boolean active
    ) {
        Page<PreventivePlan> preventivePlans = preventivePlanRepository.findByActive(
                active,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlans.getContent()
                        .stream()
                        .map(PreventivePlanMapper::toResponse)
                        .toList(),
                preventivePlans.getNumber(),
                preventivePlans.getSize(),
                preventivePlans.getTotalElements(),
                preventivePlans.getTotalPages()
        );
    }

    /**
     * Obtiene una página de planes preventivos cuyo nombre contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de buscar, sin distinguir entre
     * mayúsculas y minúsculas. Los resultados se ordenan ascendentemente por identificador para
     * que sean estables entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @param name texto parcial con el que filtrar el nombre
     * @return página de planes preventivos coincidentes; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanResponse> getPreventivePlansByName(
            int page,
            int size,
            String name
    ) {
        Page<PreventivePlan> preventivePlans = preventivePlanRepository.findByNameContainingIgnoreCase(
                name.strip(),
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventivePlans.getContent()
                        .stream()
                        .map(PreventivePlanMapper::toResponse)
                        .toList(),
                preventivePlans.getNumber(),
                preventivePlans.getSize(),
                preventivePlans.getTotalElements(),
                preventivePlans.getTotalPages()
        );
    }

    /**
     * Obtiene una página de planes preventivos cuya próxima fecha prevista está comprendida en un
     * rango de fechas inclusivo.
     *
     * <p>El rango debe situar la fecha inicial antes o en la misma fecha que la final. Los
     * resultados se ordenan ascendentemente por identificador para que sean estables entre
     * páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de planes preventivos por página
     * @param from fecha inicial del rango, incluida
     * @param to fecha final del rango, incluida
     * @return página de planes preventivos planificados en el rango; su contenido puede estar
     * vacío
     * @throws InvalidPreventivePlanDateRangeException si {@code from} es posterior a {@code to}
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventivePlanResponse> getPreventivePlansByNextDueDate(
            int page,
            int size,
            LocalDate from,
            LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new InvalidPreventivePlanDateRangeException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        Page<PreventivePlan> preventivePlans =
                preventivePlanRepository.findByNextDueDateBetween(
                        from,
                        to,
                        PageRequest.of(page, size, Sort.by("id").ascending())
                );

        return new PageResponse<>(
                preventivePlans.getContent().stream()
                        .map(PreventivePlanMapper::toResponse)
                        .toList(),
                preventivePlans.getNumber(),
                preventivePlans.getSize(),
                preventivePlans.getTotalElements(),
                preventivePlans.getTotalPages()
        );
    }
}
