package eus.birtlh.g3.user.repository;

import eus.birtlh.g3.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de persistencia para la entidad {@link User}.
 *
 * <p>Hereda las operaciones CRUD proporcionadas por Spring Data JPA.</p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Comprueba si ya existe un usuario con la dirección de correo electrónico indicada.
     *
     * @param email dirección de correo electrónico normalizada que se desea comprobar
     * @return {@code true} si el correo ya está asignado a un usuario; {@code false} en caso
     * contrario
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si el correo electrónico ya está asignado a un usuario distinto del indicado.
     *
     * @param email dirección de correo electrónico normalizada que se desea comprobar
     * @param id identificador del usuario que se excluye de la comprobación
     * @return {@code true} si el correo pertenece a otro usuario; {@code false} en caso contrario
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca usuarios cuyo correo electrónico contiene el texto indicado, sin distinguir
     * mayúsculas de minúsculas.
     *
     * @param email término parcial normalizado con el que filtrar
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de usuarios coincidentes; puede no contener resultados
     */
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    /**
     * Busca usuarios según su estado de acceso.
     *
     * @param enabled {@code true} para usuarios habilitados; {@code false} para deshabilitados
     * @param pageable configuración de página, tamaño y orden solicitados
     * @return página de usuarios con el estado indicado; puede no contener resultados
     */
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
}
