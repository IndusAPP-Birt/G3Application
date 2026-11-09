package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

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

    /**
     * Busca los planes preventivos según su estado de activación.
     *
     * @param active {@code true} para los planes activos; {@code false} para los inactivos
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de planes preventivos con el estado solicitado; puede no contener resultados
     */
    Page<PreventivePlan> findByActive(Boolean active, Pageable pageable);

    /**
     * Busca los planes preventivos cuyo nombre contiene el texto indicado, sin distinguir entre
     * mayúsculas y minúsculas.
     *
     * @param name texto parcial del nombre con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de planes preventivos coincidentes; puede no contener resultados
     */
    Page<PreventivePlan> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Busca los planes preventivos cuya próxima fecha prevista está comprendida en el rango
     * indicado, incluidos ambos límites.
     *
     * @param from fecha inicial del rango
     * @param to fecha final del rango
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de planes preventivos planificados en el rango; puede no contener resultados
     */
    Page<PreventivePlan> findByNextDueDateBetween(
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}
