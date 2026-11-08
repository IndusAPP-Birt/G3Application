package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link PreventivePlan}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventivePlanRepository extends JpaRepository<PreventivePlan, Long> {
}
