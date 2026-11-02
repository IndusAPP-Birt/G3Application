package eus.birtlh.g3.user.repository;

import eus.birtlh.g3.user.User;
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
}
