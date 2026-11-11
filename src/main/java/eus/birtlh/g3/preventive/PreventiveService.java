package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.dto.PreventiveResponse;
import eus.birtlh.g3.preventive.exceptions.PreventiveNotFoundException;
import eus.birtlh.g3.preventive.mapper.PreventiveMapper;
import eus.birtlh.g3.preventive.repository.PreventiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que coordina los casos de uso de ejecuciones preventivas.
 *
 * <p>Obtiene entidades {@link Preventive} y las transforma en DTOs públicos antes de devolverlas
 * a la capa HTTP.</p>
 */
@Service
@RequiredArgsConstructor
public class PreventiveService {

    private final PreventiveRepository preventiveRepository;

    /**
     * Obtiene una página de preventivos como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas. La transacción mantiene disponibles el plan y el inspector asociados durante
     * la conversión mediante {@link PreventiveMapper}.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @return página de preventivos públicos; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getAllPreventives(int page, int size) {
        Page<Preventive> preventives = preventiveRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene un preventivo por su identificador como DTO de respuesta.
     *
     * @param id identificador del preventivo solicitado
     * @return DTO público del preventivo encontrado
     * @throws PreventiveNotFoundException si no existe un preventivo con el identificador indicado
     */
    @Transactional(readOnly = true)
    public PreventiveResponse getPreventiveById(Long id) {
        Preventive preventive = preventiveRepository.findById(id)
                .orElseThrow(() -> new PreventiveNotFoundException(
                        "Preventivo con " + id + " no encontrado."
                ));

        return PreventiveMapper.toResponse(preventive);
    }

    /**
     * Obtiene una página de preventivos asociados a un plan preventivo.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param preventivePlanId identificador del plan preventivo con el que filtrar
     * @return página de preventivos del plan; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByPreventivePlanId(
            int page,
            int size,
            long preventivePlanId
    ) {
        Page<Preventive> preventives = preventiveRepository.findByPreventivePlanId(
                preventivePlanId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene una página de preventivos según su estado.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param status estado con el que filtrar los preventivos
     * @return página de preventivos con el estado indicado; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByStatus(
            int page,
            int size,
            PreventiveStatus status
    ) {
        Page<Preventive> preventives = preventiveRepository.findByStatus(
                status,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }

    /**
     * Obtiene una página de preventivos asignados a un inspector.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de preventivos por página
     * @param inspectorId identificador del inspector con el que filtrar
     * @return página de preventivos asignados al inspector; su contenido puede estar vacío
     */
    @Transactional(readOnly = true)
    public PageResponse<PreventiveResponse> getPreventivesByAssignedInspectorId(
            int page,
            int size,
            long inspectorId
    ) {
        Page<Preventive> preventives = preventiveRepository.findByAssignedInspectorId(
                inspectorId,
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                preventives.getContent()
                        .stream()
                        .map(PreventiveMapper::toResponse)
                        .toList(),
                preventives.getNumber(),
                preventives.getSize(),
                preventives.getTotalElements(),
                preventives.getTotalPages()
        );
    }
}
