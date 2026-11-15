package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.Preventive;
import eus.birtlh.g3.preventive.PreventiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

/**
 * Repositorio de persistencia para la entidad {@link Preventive}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventiveRepository extends JpaRepository<Preventive, Long> {

    /**
     * Busca los preventivos generados desde asignaciones de una plantilla preventiva.
     *
     * @param preventivePlanId identificador de la plantilla con la que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos de la plantilla; puede no contener resultados
     */
    Page<Preventive> findByPreventivePlanAssignmentPreventivePlanId(Long preventivePlanId, Pageable pageable);

    /**
     * Busca los preventivos generados desde asignaciones configuradas para una máquina.
     *
     * @param planId identificador de la máquina con la que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos de la máquina; puede no contener resultados
     */
    Page<Preventive> findByPreventivePlanAssignmentMachineId(Long planId, Pageable pageable);

    /**
     * Busca los preventivos según su estado.
     *
     * @param status estado con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos con el estado indicado; puede no contener resultados
     */
    Page<Preventive> findByStatus(PreventiveStatus status, Pageable pageable);

    /**
     * Busca los preventivos asignados a un inspector.
     *
     * @param inspectorId identificador del inspector con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos asignados al inspector; puede no contener resultados
     */
    Page<Preventive> findByAssignedInspectorId(Long inspectorId, Pageable pageable);

    /**
     * Busca los preventivos cuya fecha prevista está comprendida en un rango inclusivo.
     *
     * @param from fecha inicial del rango, incluida
     * @param to fecha final del rango, incluida
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos previstos en el rango; puede no contener resultados
     */
    Page<Preventive> findByDueDateBetween(LocalDate from, LocalDate to, Pageable pageable);
}
