package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import eus.birtlh.g3.preventive.mapper.PreventivePlanMapper;
import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
