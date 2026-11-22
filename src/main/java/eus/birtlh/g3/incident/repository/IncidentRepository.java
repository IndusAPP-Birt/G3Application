package eus.birtlh.g3.incident.repository;

import eus.birtlh.g3.incident.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para las entidades {@link Incident}.
 *
 * <p>Proporciona las operaciones estándar de Spring Data JPA. Incorporará las consultas
 * específicas de listado y filtrado de incidencias a medida que se expongan sus casos de uso.</p>
 */
public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
