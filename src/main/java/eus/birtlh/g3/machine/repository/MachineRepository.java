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

    /**
     * Comprueba si ya existe una máquina con el código patrimonial indicado.
     *
     * @param assetCode código patrimonial normalizado que se desea comprobar
     * @return {@code true} si el código ya está asignado a una máquina; {@code false} en caso
     * contrario
     */
    boolean existsByAssetCode(String assetCode);

    /**
     * Comprueba si ya existe una máquina con el número de serie indicado.
     *
     * @param serialNumber número de serie que se desea comprobar
     * @return {@code true} si el número de serie ya está asignado a una máquina; {@code false} en
     * caso contrario
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     * Comprueba si el número de serie ya está asignado a una máquina distinta de la indicada.
     *
     * @param serialNumber número de serie que se desea comprobar
     * @param id identificador de la máquina que se excluye de la comprobación
     * @return {@code true} si el número de serie pertenece a otra máquina; {@code false} en caso
     * contrario
     */
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    /**
     * Comprueba si el código patrimonial ya está asignado a una máquina distinta de la indicada.
     *
     * @param assetCode código patrimonial normalizado que se desea comprobar
     * @param id identificador de la máquina que se excluye de la comprobación
     * @return {@code true} si el código pertenece a otra máquina; {@code false} en caso contrario
     */
    boolean existsByAssetCodeAndIdNot(String assetCode, Long id);
}
