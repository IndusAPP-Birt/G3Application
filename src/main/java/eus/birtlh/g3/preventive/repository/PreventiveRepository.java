package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.Preventive;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link Preventive}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventiveRepository extends JpaRepository<Preventive, Long> {
}
