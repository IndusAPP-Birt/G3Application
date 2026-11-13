package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlanAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para las asignaciones de plantillas preventivas a máquinas.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA. Los métodos de consulta
 * específicos se incorporarán cuando los casos de uso de asignación, planificación o generación
 * los requieran.</p>
 */
public interface PreventivePlanAssignmentRepository extends JpaRepository<PreventivePlanAssignment, Long> {

    /**
     * Busca las asignaciones preventivas según su estado de activación.
     *
     * @param active {@code true} para las asignaciones activas; {@code false} para las inactivas
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de asignaciones con el estado solicitado; puede no contener resultados
     */
    Page<PreventivePlanAssignment> findByActive(Boolean active, Pageable pageable);

    /**
     * Busca las asignaciones preventivas configuradas para una máquina.
     *
     * @param machineId identificador de la máquina con la que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de asignaciones de la máquina; puede no contener resultados
     */
    Page<PreventivePlanAssignment> findByMachineId(Long machineId, Pageable pageable);

    /**
     * Busca las asignaciones configuradas a partir de una plantilla preventiva.
     *
     * @param preventivePlanId identificador de la plantilla con la que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de asignaciones de la plantilla; puede no contener resultados
     */
    Page<PreventivePlanAssignment> findByPreventivePlanId(Long preventivePlanId, Pageable pageable);
}
