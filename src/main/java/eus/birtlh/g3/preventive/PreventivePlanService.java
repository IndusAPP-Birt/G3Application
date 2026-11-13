package eus.birtlh.g3.preventive;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.preventive.dto.AdminCreatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.AdminUpdatePreventivePlanRequest;
import eus.birtlh.g3.preventive.dto.PreventivePlanResponse;
import eus.birtlh.g3.preventive.exceptions.PreventivePlanNotFoundException;
import eus.birtlh.g3.preventive.mapper.PreventivePlanMapper;
import eus.birtlh.g3.preventive.repository.PreventivePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que coordina los casos de uso de plantillas preventivas.
 *
 * <p>Obtiene entidades {@link PreventivePlan} y las transforma en DTOs públicos antes de
 * devolverlas a la capa HTTP. La asignación de plantillas a activos y su planificación se
 * gestionan mediante casos de uso separados.</p>
 */
@Service
@RequiredArgsConstructor
public class PreventivePlanService {

    private final PreventivePlanRepository preventivePlanRepository;

    /**
     * Obtiene una página de plantillas preventivas como DTOs de respuesta.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de plantillas por página
     * @return página de plantillas preventivas públicas; su contenido puede estar vacío
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
     * Obtiene una plantilla preventiva por su identificador como DTO de respuesta.
     *
     * @param id identificador de la plantilla preventiva solicitada
     * @return DTO público de la plantilla encontrada
     * @throws PreventivePlanNotFoundException si no existe una plantilla con el identificador
     *                                         indicado
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
     * Obtiene una página de plantillas preventivas según su disponibilidad para nuevas
     * asignaciones.
     *
     * <p>Conserva el orden ascendente por identificador para que los resultados sean estables
     * entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de plantillas por página
     * @param active {@code true} para las plantillas activas; {@code false} para las inactivas
     * @return página de plantillas con el estado solicitado; su contenido puede estar vacío
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
     * Obtiene una página de plantillas preventivas cuyo nombre contiene el texto indicado.
     *
     * <p>El término se elimina de espacios exteriores antes de buscar, sin distinguir entre
     * mayúsculas y minúsculas. Los resultados se ordenan ascendentemente por identificador para
     * que sean estables entre páginas.</p>
     *
     * @param page índice de página basado en cero
     * @param size número de plantillas por página
     * @param name texto parcial con el que filtrar el nombre
     * @return página de plantillas coincidentes; su contenido puede estar vacío
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
     * Crea una plantilla preventiva independiente de los activos.
     *
     * @param request datos validados y normalizados de la plantilla que se desea crear
     * @return DTO público de la plantilla creada
     */
    @Transactional
    public PreventivePlanResponse createPreventivePlan(AdminCreatePreventivePlanRequest request) {
        PreventivePlan preventivePlan = preventivePlanRepository.save(
                new PreventivePlan(
                        request.name(),
                        request.description()
                )
        );

        return PreventivePlanMapper.toResponse(preventivePlan);
    }

    /**
     * Actualiza completamente una plantilla preventiva existente.
     *
     * <p>Actualiza su nombre, acciones descritas y disponibilidad para nuevas asignaciones.</p>
     *
     * @param id identificador de la plantilla preventiva que se desea actualizar
     * @param request datos validados y normalizados con los que actualizar la plantilla
     * @return DTO público de la plantilla actualizada
     * @throws PreventivePlanNotFoundException si no existe una plantilla con el identificador
     *                                         indicado
     */
    @Transactional
    public PreventivePlanResponse updatePreventivePlan(Long id, AdminUpdatePreventivePlanRequest request) {
        PreventivePlan preventivePlan = preventivePlanRepository.findById(id)
                .orElseThrow(() -> new PreventivePlanNotFoundException(
                        "Plan preventivo con " + id + " no encontrado."
                ));

        preventivePlan.changeName(request.name());
        preventivePlan.changeDescription(request.description());

        if (request.active()) {
            preventivePlan.activate();
        } else {
            preventivePlan.deactivate();
        }

        return PreventivePlanMapper.toResponse(preventivePlan);
    }
}
