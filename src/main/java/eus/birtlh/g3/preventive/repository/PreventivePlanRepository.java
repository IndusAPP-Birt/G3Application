package eus.birtlh.g3.preventive.repository;

import eus.birtlh.g3.preventive.PreventivePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para las plantillas {@link PreventivePlan}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface PreventivePlanRepository extends JpaRepository<PreventivePlan, Long> {

    /**
     * Busca plantillas preventivas según su disponibilidad para nuevas asignaciones.
     *
     * @param active {@code true} para las plantillas activas; {@code false} para las inactivas
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de plantillas con el estado solicitado; puede no contener resultados
     */
    Page<PreventivePlan> findByActive(Boolean active, Pageable pageable);

    /**
     * Busca plantillas preventivas cuyo nombre contiene el texto indicado, sin distinguir entre
     * mayúsculas y minúsculas.
     *
     * @param name texto parcial del nombre con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de plantillas coincidentes; puede no contener resultados
     */
    Page<PreventivePlan> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
