package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.Preventive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link Preventive}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventiveRepository extends JpaRepository<Preventive, Long> {

    /**
     * Busca los preventivos asociados a un plan preventivo.
     *
     * @param id identificador del plan preventivo con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de preventivos del plan; puede no contener resultados
     */
    Page<Preventive> findByPreventivePlanId(Long id, Pageable pageable);
}
