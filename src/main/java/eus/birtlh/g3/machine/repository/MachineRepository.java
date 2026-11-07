package eus.birtlh.g3.machine.repository;

import eus.birtlh.g3.machine.Machine;
import eus.birtlh.g3.machine.MachineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de persistencia para la entidad {@link Machine}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface MachineRepository extends JpaRepository<Machine, Long> {

    /**
     * Busca una máquina por su código patrimonial único.
     *
     * @param assetCode código patrimonial normalizado que identifica la máquina
     * @return máquina encontrada o un {@link Optional} vacío si no existe
     */
    Optional<Machine> findByAssetCode(String assetCode);

    /**
     * Busca máquinas según su estado operativo.
     *
     * @param status estado con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de máquinas con el estado indicado; puede no contener resultados
     */
    Page<Machine> findByStatus(MachineStatus status, Pageable pageable);
}
