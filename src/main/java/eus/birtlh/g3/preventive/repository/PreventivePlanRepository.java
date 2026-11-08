package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para la entidad {@link PreventivePlan}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventivePlanRepository extends JpaRepository<PreventivePlan, Long> {

    Page<PreventivePlan> findByMachineId(Long machineId, PageRequest pageRequest);
}
