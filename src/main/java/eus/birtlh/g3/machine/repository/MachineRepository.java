package eus.birtlh.g3.machine.repository;

import eus.birtlh.g3.machine.Machine;
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
}
