package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link PreventivePlan}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventivePlanRepository extends JpaRepository<PreventivePlan, Long> {

    /**
     * Busca los planes preventivos asociados a una máquina.
     *
     * @param machineId identificador de la máquina con la que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de planes preventivos de la máquina; puede no contener resultados
     */
    Page<PreventivePlan> findByMachineId(Long machineId, Pageable pageable);
}
