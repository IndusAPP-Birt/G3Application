package eus.birtlh.g3.machine.repository;

import eus.birtlh.g3.machine.Machine;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link Machine}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface MachineRepository extends JpaRepository<Machine, Long> {
}
