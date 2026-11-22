package eus.birtlh.g3.incident.repository;

import eus.birtlh.g3.incident.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para las entidades {@link Incident}.
 *
 * <p>Proporciona las operaciones estándar de Spring Data JPA y las consultas derivadas para
 * listar incidencias según su relación con los preventivos.</p>
 */
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * Obtiene una página de incidencias asociadas a un preventivo.
     *
     * @param preventiveId identificador del preventivo de origen
     * @param pageable configuración de página y ordenación solicitada
     * @return página de incidencias vinculadas al preventivo; puede estar vacía
     */
    Page<Incident> findAllByPreventiveId(Long preventiveId, Pageable pageable);

    /**
     * Obtiene una página de incidencias asignadas a un técnico de mantenimiento.
     *
     * @param technicianEmail correo electrónico del técnico asignado
     * @param pageable configuración de página y ordenación solicitada
     * @return página de incidencias asignadas al técnico; puede estar vacía
     */
    Page<Incident> findByAssignedTechnicianEmail(String technicianEmail, Pageable pageable);
}
